package tf.veriny.ss76.engine.system

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.scene.builder.SceneBuilder
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Responsible for building the checkpoint scene.
 */
public class CheckpointSceneManager(private val state: EngineState) {
    public companion object {
        public const val CHECKPOINT_SCENE_NAME: String = "engine.checkpoints"
        private val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss")
    }

    private inner class CheckpointButton(val idx: Int, val load: Boolean) : Button {
        override val name: String = if (load) "load-$idx" else "save-$idx"

        override fun run(state: EngineState) {
            if (load) {
                state.saveManager.load(idx)
            } else {
                state.saveManager.save(idx)
            }

            rebuild()
        }
    }

    internal fun rebuild() {
        val scene = SceneBuilder(state, "engine.checkpoints")
        scene.page {
            addFragment("engine.checkpoint.header")
            repeat(2) { addFragment("engine.generic.newline") }

            repeat(5) { idx ->
                val time = state.saveManager.getSaveDate(idx)
                addFragment("engine.checkpoint.slot${idx}")
                scene.addButton(CheckpointButton(idx, load = false))

                if (time == null) {
                    addFragment("engine.checkpoint.empty")
                    addRawFragment("- $[@=salmon,`=save-$idx] ")
                    addFragment("engine.checkpoint.save")
                    addRawFragment("$[pop=]")
                } else {
                    val zone = ZoneId.systemDefault()
                    val it = time.toInstant().atZone(zone)
                    val ts = it.format(FORMATTER)

                    addRawFragment("$[@=sky] $ts $[pop=]")
                    addRawFragment("- $[@=salmon,`=save-$idx] ")
                    addFragment("engine.checkpoint.save")
                    addRawFragment("$[pop=] / ")
                    addRawFragment("- $[@=salmon,`=load-$idx] ")
                    addFragment("engine.checkpoint.load")
                    addRawFragment("$[pop=]")

                    scene.addButton(CheckpointButton(idx, load = true))
                }

                addFragment("engine.generic.newline")
            }
        }

        state.sceneManager.registerScene(scene.get(), suppressWarning = true)
        if (state.sceneManager.currentSceneIs(CHECKPOINT_SCENE_NAME)) {
            state.sceneManager.rebake()
        }
    }
}