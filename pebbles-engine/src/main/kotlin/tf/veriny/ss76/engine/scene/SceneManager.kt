/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import okio.BufferedSink
import okio.BufferedSource
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.SS76EngineInternalError
import tf.veriny.ss76.engine.Saveable
import tf.veriny.ss76.engine.nvl.NVLScreen
import tf.veriny.ss76.engine.readPascalString
import tf.veriny.ss76.engine.writePascalString

// TODO: Clean this up a bit

/**
 * Responsible for handling loaded scenes.
 */
public class SceneManager(
    private val state: EngineState,
) : Saveable {
    private val seenScenes /*on the sea shore*/ = mutableSetOf<String>()
    private val completedScenes = mutableSetOf<String>()
    public val registeredScenes: MutableMap<String, SceneDefinition> = mutableMapOf()

    public val linkHelper: NodeLinkHelper = NodeLinkHelper(state)

    public val registeredSceneCount: Int get() = registeredScenes.size

    // stack of renderers, *not* definitions.
    private val stack = ArrayDeque<SceneState>()

    /** The number of scenes on the current scene stack. */
    public val stackSize: Int get() = stack.size

    // current *renderer*, not definitions.
    public val currentScene: SceneState get() = stack.last()

    private var previousScene: VirtualNovelSceneDefinition? = null
        private set(value) {
            value?.let { completedScenes.add(it.sceneId) }
            field = value
        }

    /** Registers a single scene. */
    public fun registerScene(scene: SceneDefinition) {
        registeredScenes[scene.sceneId] = scene
    }

    /**
     * Re-registers a scene. This will update any references in the current stack of scenes.
     */
    public fun reregisterScene(scene: SceneDefinition) {
        val old = registeredScenes[scene.sceneId]
        registeredScenes[scene.sceneId] = scene

        if (old == null) return
        if (stack.isEmpty()) return

        when {
            // TOS requires unloading the old scene and replacing it with the new one
            currentScene.definition == old -> {
                stack.removeLast()
                val state = SceneState(this.state, scene)
                stack.add(state)
                activateScene(state)
            }
            // just swap the references to the old scenes with the new one
            previousScene == old -> {
                previousScene = scene
            }

            else -> {
                for (idx in 0 until stackSize) {
                    if (stack[idx].definition == old) {
                        stack[idx] = SceneState(this.state, definition = scene)
                    }
                }
            }
        }
    }

    /**
     * Checks if this scene has been completed before. Scenes are marked as completed when exited
     * from; this persists across checkpoints.
     */
    public fun hasCompletedScene(scene: String): Boolean {
        return scene in completedScenes
    }

    /** Checks if this scene is registered. */
    public fun doesSceneExist(scene: String): Boolean = registeredScenes[scene] != null

    /** Checks if this scene has been visited before. */
    public fun hasVisitedScene(scene: String): Boolean = scene in seenScenes

    /** Checks if this scene has been visited before. */
    public fun hasVisitedScene(scene: VirtualNovelSceneDefinition): Boolean =
        hasVisitedScene(scene.sceneId)

    /** Checks if the current scene has the specified ID. */
    public fun currentSceneIs(id: String): Boolean = currentScene.definition.sceneId == id

    // == Scene stack == //

    /**
     * Activates a scene, swapping out the current scene data and screen.
     */
    private fun activateScene(scene: SceneState) {
        println("activating ${scene.definition.sceneId}")
        seenScenes.add(scene.definition.sceneId)

        if (scene.definition.modifiers.nonRenderable) {
            // skip creating a screen
            scene.definition.onLoad(scene)
            if (stack.last() == scene) {
                throw SS76EngineInternalError(
                    "Scene '${scene.definition.sceneId} is marked as non-renderable, " +
                    "but failed to switch scene on load"
                )
            }
            return
        }

        val forceNvl = System.getProperty("disable-adv-renderers", "false").toBooleanStrict()
        val advMode = scene.definition.createAdvRenderer()
        if (!forceNvl && advMode != null) {
            val screen = state.screenManager.currentScreen
            TODO("reimplement ADV mode")
            /*if (screen !is ADVScreen || !screen.isAlreadyRendering(advMode)) {
                state.screenManager.changeScreen(ADVScreen(state, advMode))
            }*/
        } else {
            if (scene.definition.modifiers.causesFadeIn) {
                println("fading in scene ${scene.definition.sceneId}")
                // 30 frames of fade-in
                scene.timer = -30
                state.screenManager.fadeIn(NVLScreen(state, scene))
            } else {
                state.screenManager.changeScreen(NVLScreen(state, scene))
            }
        }

        scene.definition.onLoad(scene)
    }

    /**
     * Pushes and activates a scene.
     */
    public fun pushScene(scene: VirtualNovelSceneDefinition) {
        if (stack.isNotEmpty()) {
            val tos = stack.last()
            previousScene = tos.definition
        }

        val state = SceneState(this.state, scene)
        stack.add(state)
        activateScene(state)
    }

    /**
     * Pushes and activates a scene, using its string id.
     */
    public fun pushScene(scene: String) {
        val realScene = getRegisteredScene(scene)
        pushScene(realScene)
    }

    /**
     * Changes the top scene on the stack.
     */
    public fun changeScene(scene: SceneDefinition) {
        val tos = stack.removeLast()
        previousScene = tos.definition

        val state = SceneState(this.state, scene)
        stack.add(state)
        activateScene(state)
    }

    /**
     * Changes the top scene on the stack, using its ID.
     */
    public fun changeScene(scene: String) {
        val definition = getRegisteredScene(scene)
        changeScene(definition)
    }

    /**
     * Exits from the top scene.
     */
    public fun exitScene() {
        val tos = stack.removeLast()
        previousScene = tos.definition

        val newTos = stack.last()
        activateScene(newTos)
    }

    /**
     * Gets a single scene.
     */
    public fun getRegisteredScene(scene: String): SceneDefinition {
        return registeredScenes[scene] ?: error("missing scene definition: $scene")
    }

    // == Saving == //
    override fun write(buffer: BufferedSink) {
        // write seen scenes
        val seenCount = seenScenes.size
        buffer.writeInt(seenCount)
        for (scene in seenScenes) {
            buffer.writePascalString(scene)
        }

        val completedCount = completedScenes.size
        buffer.writeInt(completedCount)
        for (scene in completedScenes) {
            buffer.writePascalString(scene)
        }

        // write the current scene stack
        val scenes = stack.map { it.definition.sceneId }
        buffer.writeInt(scenes.size)
        for (scene in scenes) {
            buffer.writePascalString(scene)
        }
    }

    internal fun printSceneStack() {
        for ((idx, scene) in this.stack.withIndex()) {
            println("  #$idx: id '${scene.definition.sceneId}'")
        }
    }

    override fun read(buffer: BufferedSource) {
        seenScenes.clear()
        completedScenes.clear()
        stack.clear()

        val seenCount = buffer.readInt()
        for (idx in 0 until seenCount) {
            val sceneId = buffer.readPascalString()
            seenScenes.add(sceneId)
        }

        val completedCount = buffer.readInt()
        for (idx in 0 until completedCount) {
            val sceneId = buffer.readPascalString()
            seenScenes.add(sceneId)
        }

        val sceneCount = buffer.readInt()
        for (idx in 0 until sceneCount) {
            pushScene(buffer.readPascalString())
        }

        printSceneStack()
    }
}
