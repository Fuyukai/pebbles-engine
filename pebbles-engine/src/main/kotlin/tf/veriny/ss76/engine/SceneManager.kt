/*
 * This file is part of Pebbles.
 *
 * Pebbles is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pebbles is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pebbles.  If not, see <https://www.gnu.org/licenses/>.
 */

package tf.veriny.ss76.engine

import okio.BufferedSink
import okio.BufferedSource
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.adv.ADVScreen
import tf.veriny.ss76.engine.nvl.NVLScreen
import tf.veriny.ss76.engine.scene.Inventory
import tf.veriny.ss76.engine.scene.SceneDefinition
import tf.veriny.ss76.engine.scene.SceneState
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*

/**
 * Responsible for handling loaded scenes.
 */
public class SceneManager(
    public val state: EngineState,
    public val namespace: String
) : Saveable {

    private val seenScenes /*on the sea shore*/= mutableSetOf<String>()
    public val registeredScenes: MutableMap<String, SceneDefinition> = mutableMapOf()
    public val sceneCount: Int get() = registeredScenes.size

    // stack of renderers, *not* definitions.
    private val stack = ArrayDeque<SceneState>()
    public val stackSize: Int get() = stack.size
    // current *renderer*, not definitions.
    public val currentScene: SceneState get() = stack.last()

    private var previousScene: SceneDefinition? = null

    /** The inventory system instance. */
    public val inventory: Inventory = Inventory(this)

    init {
        inventory.changeState(0)
    }

    public fun currentSceneIs(id: String): Boolean = currentScene.definition.id == id

    /**
     * Registers a single scene.
     */
    public fun registerScene(scene: SceneDefinition) {
        registeredScenes[scene.id] = scene
    }

    /**
     * Re-registers a scene. This will update any references in the current stack of scenes.
     */
    public fun reregisterScene(scene: SceneDefinition) {
        val old = registeredScenes[scene.id]
        registeredScenes[scene.id] = scene

        if (old == null) return
        if (stack.isEmpty()) return

        when {
            // TOS requires unloading the old scene and replacing it with the new one
            currentScene.definition == old -> {
                stack.removeLast()
                val state = SceneState(this.state, scene)
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
     * Saves the previously seen scenes.
     */
    public fun saveSeenScenes() {
        val dataDir = CheckpointManager.BASE_DIR
        val saveDir = dataDir.resolve(namespace)
        Files.createDirectories(saveDir)

        val saveData = saveDir.resolve("seen.txt")
        val savedScenes = seenScenes.joinToString("\n")
        Files.writeString(saveData, savedScenes, CREATE, TRUNCATE_EXISTING)
    }

    /**
     * Loads the previous seen scenes.
     */
    public fun loadSeenScenes() {
        val dataDir = CheckpointManager.BASE_DIR
        val saveDir = dataDir.resolve(namespace)
        Files.createDirectories(saveDir)

        val saveData = saveDir.resolve("seen.txt")
        if (!Files.exists(saveData)) return
        val scenes = Files.readString(saveData).split("\n")
        seenScenes.addAll(scenes)
    }

    public fun hasVisitedScene(scene: String): Boolean {
        return seenScenes.contains(scene)
    }

    public fun hasVisitedScene(scene: SceneDefinition): Boolean = hasVisitedScene(scene.id)

    // == Scene stack == //

    private fun activateScene(scene: SceneState) {
        println("activating ${scene.definition.id}")
        seenScenes.add(scene.definition.id)
        saveSeenScenes()

        if (scene.definition.linkedInventoryId > 0) {
            inventory.changeState(scene.definition.linkedInventoryId)
        }

        val forceNvl = System.getProperty("disable-adv-renderers", "false").toBooleanStrict()
        val advMode = scene.definition.advSubRenderer
        if (!forceNvl && advMode != null) {
            val screen = state.screenManager.currentScreen
            if (screen !is ADVScreen || !screen.isAlreadyRendering(advMode)) {
                state.screenManager.changeScreen(ADVScreen(state, advMode))
            }
        } else {
            state.screenManager.setNvlScreen()
        }

        scene.definition.onLoadHandlers.forEach { it.invoke(scene) }
    }

    /**
     * Pushes and activates a scene.
     */
    public fun pushScene(scene: SceneDefinition) {
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
        val realScene = getScene(scene)
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
        val scene = getScene(scene)
        changeScene(scene)
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
    public fun getScene(scene: String): SceneDefinition {
        return registeredScenes[scene] ?: error("missing scene definition: $scene")
    }

    // == Saving == //
    override fun write(buffer: BufferedSink) {
        val scenes = stack.map { it.definition.id }
        buffer.writeInt(scenes.size)
        for (scene in scenes) {
            buffer.writePascalString(scene)
        }
    }

    private fun printSceneStack() {
        println("scene stack:")
        for ((idx, scene) in this.stack.withIndex()) {
            println("  #$idx: id '${scene.definition.id}'")
        }
    }

    override fun read(buffer: BufferedSource) {
        stack.clear()

        val sceneCount = buffer.readInt()
        for (idx in 0 until sceneCount) {
            val size = buffer.readInt()
            val sceneId = buffer.readUtf8(size.toLong())
            pushScene(sceneId)
        }

        printSceneStack()
    }

    // == Debug == //
    public fun writeAllSceneData() {}

    // == Input == //
}