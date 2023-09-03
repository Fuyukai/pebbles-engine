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
import tf.veriny.ss76.engine.saving.Saveable
import tf.veriny.ss76.engine.nvl.NVLScreen
import tf.veriny.ss76.engine.psm.SceneBaker
import tf.veriny.ss76.engine.psm.UnbakedScene
import tf.veriny.ss76.engine.readPascalString
import tf.veriny.ss76.engine.writePascalString

/**
 * Responsible for loading and baking scenes.
 */
public class SceneManager(internal val state: EngineState) : Saveable {
    /**
     * The scene bakery that turns raw unbaked scene text into baked scenes.
     */
    public val sceneBakery: SceneBaker = SceneBaker()

    /** The mapping of scene ID to unbaked scene. */
    private val sceneMapping = mutableMapOf<String, UnbakedScene>()

    /** The stack of scenes currently running. */
    private val sceneStack = ArrayDeque<SceneState>()

    /** The set of scenes that have already been viewed. */
    private val seenScenes = mutableSetOf<String>()

    /** Used for dealing with links. */
    public val linkHelper: NodeLinkHelper = NodeLinkHelper(state)

    /** The previous scene. Used for tracking seen scenes. */
    private var previousScene: SceneState? = null
        private set(value) {
            value?.let { seenScenes.add(it.definition.sceneId) }
            field = value
        }

    private val loadedIds = ArrayDeque<String>()

    /** The number of scenes currently on the scene stack. */
    public val stackSize: Int get() = sceneStack.size

    /** The number of registered scenes. */
    public val registeredScenes: Int get() = sceneMapping.size

    /** The current scene running. */
    public val currentScene: SceneState get() = sceneStack.last()

    /**
     * Checks if the current scene has the specified scene ID.
     */
    public fun currentSceneIs(sceneId: String): Boolean {
        return sceneStack.lastOrNull()?.definition?.sceneId == sceneId
    }

    /**
     * Checks if we have seen the specified scene before.
     */
    public fun hasSeenScene(sceneId: String): Boolean {
        return seenScenes.contains(sceneId)
    }

    /**
     * Registers a new unbaked scene with this scene manager.
     */
    public fun registerScene(unbakedScene: UnbakedScene, suppressWarning: Boolean = false) {
        if (sceneMapping.put(unbakedScene.sceneId, unbakedScene) != null) {
            if (!suppressWarning) println("SCENES: scene ${unbakedScene.sceneId} already existed, overwriting")
        }
    }

    /**
     * Activates a new scene, pushing this scene onto the top of the scene stack.
     */
    private fun activateScene(state: SceneState) {
        println("activating ${state.definition.sceneId}")
        sceneStack.addLast(state)

        if (state.definition.modifiers.nonRenderable) {
            // skip creating a screen
            state.definition.onLoad(state)
            if (sceneStack.last() == state) {
                throw SS76EngineInternalError(
                    "Scene '${state.definition.sceneId} is marked as non-renderable, " +
                        "but failed to switch scene on load"
                )
            }
            return
        }

        val advMode = state.definition.createAdvRenderer()
        if (advMode != null) {
            val screen = this.state.screenManager.currentScreen
            TODO("reimplement ADV mode")
            /*if (screen !is ADVScreen || !screen.isAlreadyRendering(advMode)) {
                state.screenManager.changeScreen(ADVScreen(state, advMode))
            }*/
        } else {
            if (state.definition.modifiers.causesFadeIn) {
                println("fading in scene ${state.definition.sceneId}")
                // 30 frames of fade-in
                state.timer = -30
                this.state.screenManager.fadeIn(NVLScreen(this.state, state))
            } else {
                this.state.screenManager.changeScreen(NVLScreen(this.state, state))
            }
        }

        state.definition.onLoad(state)
    }

    internal fun prebakeScenes() {
        for (scene in sceneMapping.values) {
            if (!scene.isStatic) continue
            scene.bake(state, isPreBaking = true, force = false)
        }
    }

    private fun loadAndActivateScene(id: String, force: Boolean = false): SceneState {
        val unbaked = sceneMapping[id] ?: error("No such registered scene: $id")
        val definition = unbaked.bake(
            state, isPreBaking = false, force = force,
        )
        val sceneState = SceneState(state, definition)
        activateScene(sceneState)
        return sceneState
    }

    /**
     * Pushes a new scene onto the scene stack.
     */
    public fun pushScene(id: String) {
        previousScene = sceneStack.lastOrNull()
        loadAndActivateScene(id)
    }

    /**
     * Swaps out the scene on the top of the stack with the specified scene.
     */
    public fun swapScene(id: String) {
        previousScene = sceneStack.removeLastOrNull()
        loadAndActivateScene(id)
    }

    /**
     * Rebakes the top-most scene.
     */
    public fun rebake() {
        val scene = sceneStack.removeLastOrNull() ?: error("no scene to rebake!")
        val id = scene.definition.sceneId
        val newScene = loadAndActivateScene(id, force = true)
        newScene.timer = scene.timer
    }

    /**
     * Exits the top-most scene.
     */
    public fun exitScene() {
        val tos = sceneStack.removeLast()
        previousScene = tos

        val newTos = sceneStack.lastOrNull()
            ?: throw SS76EngineInternalError("attempted to exit last scene!")
        activateScene(newTos)
    }

    override fun read(buffer: BufferedSource) {
        seenScenes.clear()
        sceneStack.clear()

        val sceneCount = buffer.readInt()
        for (idx in 0 until sceneCount) {
            loadedIds.addLast(buffer.readPascalString())
        }

        val seenCount = buffer.readInt()
        for (idx in 0 until seenCount) {
            val sceneId = buffer.readPascalString()
            seenScenes.add(sceneId)
        }
    }

    override fun write(buffer: BufferedSink) {
        val scenes = sceneStack.map { it.definition.sceneId }
        buffer.writeInt(scenes.size)
        for (scene in scenes) {
            buffer.writePascalString(scene)
        }

        val seenCount = seenScenes.size
        buffer.writeInt(seenCount)
        for (scene in seenScenes) {
            buffer.writePascalString(scene)
        }
    }

    // do this here so that scene baking uses the checkpoint's event flags instead. that way
    // there's no implicit load order dependency on the event flag manager.
    override fun postLoad() {
        for (id in loadedIds) pushScene(id)
    }

    /**
     * Clears the current scene stack.
     */
    internal fun clear() {
        sceneStack.clear()
        previousScene = null
    }
}