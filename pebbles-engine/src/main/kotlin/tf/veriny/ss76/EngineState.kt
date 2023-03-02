/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import ktx.app.KtxInputAdapter
import tf.veriny.ss76.engine.*
import tf.veriny.ss76.engine.font.FontManager
import tf.veriny.ss76.engine.renderer.OddCareRenderer
import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.createAndRegisterScene
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.system.CheckpointScene
import tf.veriny.ss76.engine.system.SYSTEM_STARTUP_NAME
import tf.veriny.ss76.engine.system.registerSystemScenes
import tf.veriny.ss76.engine.util.EktFiles
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * The EngineState object wraps systems required to run the SS76 engine.
 */
public class EngineState(public val settings: SS76Settings) {
    private val demoRenderer = OddCareRenderer()

    /** Used to conditionally enable or disable debugging content. */
    public var isDebugMode: Boolean = settings.isDebugMode

    /** The global frame timer. Increments monotonically by one every frame. */
    public var globalTimer: Long = 0L
        private set

    /** Used for mapping YAML content. */
    public val yamlLoader: ObjectMapper = ObjectMapper(YAMLFactory())

    /** Used for manging assets. */
    public val assets: EngineAssetManager = EngineAssetManager()

    /** The font manager for rendering text. */
    public val fontManager: FontManager = FontManager(this)

    /** The save manager, responsible for saving things. */
    public val saveManager: SaveManager = SaveManager(this)

    /** The scene manager for Virtual Novel scenes. */
    public val sceneManager: SceneManager = SceneManager(this)

    /** The event flag manager. */
    public val eventFlagsManager: EventFlags = EventFlags(this)

    /** The screen manager, used to swap out the current renderer. */
    public val screenManager: ScreenManager = ScreenManager(this)

    public val musicManager: MusicManager = MusicManager(this)

    init {
        yamlLoader.apply {
            registerKotlinModule()
            registerModules()
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

        saveManager.addSaveable("event-flags", eventFlagsManager)
        saveManager.addSaveable("scenes", sceneManager)
    }

    /** The input multiplexer, used for input. */
    public val input: InputMultiplexer = SafeMultiplexer(this, object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            // helper functionality that overrides all sub-screens.
            when (keycode) {
                Input.Keys.F1 -> {
                    if (isDebugMode) {
                        openDataScene()
                    }
                }

                Input.Keys.F2 -> {
                    // push demo UI
                    if (SS76.IS_DEMO || isDebugMode) {
                        repeat(sceneManager.stackSize - 1) { sceneManager.exitScene() }
                        sceneManager.changeScene("demo-meta-menu")
                    }
                }

                Input.Keys.F3 -> {

                }
                // reserved
                Input.Keys.F4 -> {}
                Input.Keys.F5 -> {}
                else -> return super.keyDown(keycode)
            }

            return true
        }
    })

    private fun openDataScene() {
        val scene = sceneManager.createAndRegisterScene("engine.data-scene") {
            onLoad { it.timer = 99999 }

            page {
                line("SS76 Engine Info Scene")
                newline()

                line("Registered scenes: ${sceneManager.registeredScenes.size}")
                line("Global timer: $globalTimer")
                var memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                memoryUsage /= (1024 * 1024)
                line("Memory usage: $memoryUsage MiB")
            }
        }

        if (sceneManager.currentSceneIs("engine.data-scene")) {
            sceneManager.changeScene(scene)
        } else {
            sceneManager.pushScene(scene)
        }
    }

    /**
     * Called when LibGDX creates the engine.
     */
    @OptIn(ExperimentalTime::class)
    internal fun created() {
        println("Loading engine state....")
        val fullTime = measureTime {

            val fontGenTime = measureTime {
                fontManager.generateAllFonts()
            }
            println("All fonts generated in $fontGenTime.")

            val checkpoints = CheckpointScene(this)
            checkpoints.register()

            settings.initialiser(this)

            val loadTime = measureTime { assets.autoload() }
            println("Auto-loaded all assets in $loadTime.")
            EktFiles.RESOLVER.closeAllFilesystems()

            var scene = settings.startupScene ?: System.getProperty("ss76.scene")
            if (scene == null) {
                scene = if (isDebugMode) "demo-meta-menu" else "main-menu"
            }

            registerSystemScenes(scene, sceneManager)
        }

        println("Initialised SS76 engine with ${sceneManager.registeredSceneCount} scenes in $fullTime.")

        sceneManager.pushScene(SYSTEM_STARTUP_NAME)
    }

    /**
     * Renders the current screen.
     */
    internal fun render() {
        screenManager.currentScreen.render(Gdx.graphics.deltaTime)
        if ((SS76.IS_DEMO || isDebugMode) && screenManager.currentScreen !is ErrorScreen) {
            demoRenderer.render()
        }

        globalTimer++
    }

}
