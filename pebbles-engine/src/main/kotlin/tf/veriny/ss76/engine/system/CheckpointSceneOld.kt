/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.system

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.Button
/*import tf.veriny.ss76.engine.scene.UpdatableSceneWrapper*/
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Responsible for saving checkpoints.
 */
public class CheckpointSceneOldUnused(
    private val state: EngineState,
) {
    public companion object {
        /** The name of the checkpoint save manager scene. */
        public const val CHECKPOINT_SCENE_NAME: String = "engine.save-menu"

    }

    /** The menu for checkpoints. */
    // private val checkpointScene = UpdatableSceneWrapper(CHECKPOINT_SCENE_NAME)

    public fun register() {
        updateCheckpointScene()
    }

    private fun updateCheckpointScene() {
        /*checkpointScene.reset()
        checkpointScene.edit(0) {
            line("@slate@Checkpoint @slate@Menu")
            newline()

            line(
                "A checkpoint saves the current state of your reading on a particular route. " +
                "When loading a checkpoint, the current scene and scene stack are restored. " +
                "Thus, you can save anywhere, no matter how deep."
            )
            newline()

            for (idx in 0 until 5) {
                val time = state.saveManager.getSaveDate(idx)
                if (time == null) {
                    line("Slot #$idx - @green@empty - @salmon@`save-$idx`SAVE")
                } else {
                    val zone = ZoneId.systemDefault()
                    val it = time.toInstant().atZone(zone)
                    val ts = it.format(formatter)

                    line("Slot #$idx - :push:@sky@ $ts :pop: - @salmon@`load-$idx`LOAD / @salmon@`save-$idx`SAVE")
                }

                checkpointScene.addButton(CheckpointButton(idx, true))
                checkpointScene.addButton(CheckpointButton(idx, false))
            }

            newline()
            backButton()
        }

        checkpointScene.register(state.sceneManager)*/
    }
}
