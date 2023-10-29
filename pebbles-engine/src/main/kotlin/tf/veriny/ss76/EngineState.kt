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
import tf.veriny.ss76.engine.psm.PsmFragmentManager
import tf.veriny.ss76.engine.renderer.OddCareRenderer
import tf.veriny.ss76.engine.saving.SaveManager
import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.system.CheckpointSceneManager
import tf.veriny.ss76.engine.system.registerAboutScene
import tf.veriny.ss76.engine.util.EktFiles
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * The EngineState object wraps systems required to run the SS76 engine.
 */
public class EngineState(
    public val settings: SS76Settings,
    public val preferencesManager: PreferencesManager,
) {
    private val oddCareRenderer: OddCareRenderer? =
        if (!settings.isDeveloperMode) null
        else OddCareRenderer("DEVELOPER MODE")

    /** The global frame timer. Increments monotonically by one every frame. */
    public var globalTimer: Long = 0L
        private set

    /** Used for mapping YAML content. */
    public val yamlLoader: ObjectMapper = ObjectMapper(YAMLFactory())

    /** Used for loading PSM data. */
    public val psmLoader: PsmFragmentManager = PsmFragmentManager(preferencesManager["language"])

    /** Used for manging assets. */
    public val assets: EngineAssetManager = EngineAssetManager(this)

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

    private var hasErrored = false

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
            // all other keys are reserved
            when (keycode) {
                Input.Keys.F1 -> {
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        sceneManager.pushScene("engine.pebbles.about")
                    }
                }

                Input.Keys.F2 -> {
                    // push demo UI
                    if (!settings.isDeveloperMode) return false

                    repeat(sceneManager.stackSize - 1) { sceneManager.exitScene() }
                    sceneManager.swapScene("demo-meta-menu")
                }

                Input.Keys.F3 -> {
                    val shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                    if (!shift) {
                        fontManager.getFontList("default")!!.cycle()
                    } else {
                        fontManager.getFontList("alt")!!.cycle()
                    }

                    sceneManager.rebake()
                }
                Input.Keys.F4 -> {
                    EktFiles.RESOLVER.closeAllFilesystems()
                }
                Input.Keys.F5 -> {}
                Input.Keys.F6 -> {
                    sceneManager.rebake()
                }
                Input.Keys.F7 -> {}
                Input.Keys.F8 -> {}
                Input.Keys.F9 -> {}
                Input.Keys.F10 -> {
                    throw SS76EngineInternalError("uh oh!")
                }
                else -> return super.keyDown(keycode)
            }

            return true
        }
    })

    private fun openDataScene() {
        /*val scene = sceneManager.createAndRegisterScene("engine.data-scene") {
            onLoad { it.timer = 99999 }

            page {
                line("SS76 Engine Info Scene")
                newline()

                line("Registered scenes: ${sceneManager.registerScene().size}")
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
        }*/
    }

    /**
     * Handles an internal SS76 error.
     */
    internal fun handleError(error: Exception) {
        if (hasErrored) {
            val err = Throwable("Already hit an error, killing ourselves")
            err.addSuppressed(error)
            throw err
        }

        hasErrored = true

        input.clear()
        screenManager.error(error)
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

            settings.assetAdditions(assets)

            val loadTime = measureTime { assets.autoload() }
            println("Auto-loaded all assets in $loadTime.")

            sceneManager.registerAboutScene()
            settings.initialiser(this)

            val checkpoints = CheckpointSceneManager(this)
            checkpoints.rebuild()

            EktFiles.RESOLVER.closeAllFilesystems()

            // val time = measureTime { sceneManager.prebakeScenes() }
            // println("Pre-baked all static scenes in ${time.inWholeMilliseconds}ms.")
        }

        println("Initialised SS76 engine with ${sceneManager.registeredScenes} scenes in $fullTime.")

        var scene = settings.startupScene ?: System.getProperty("ss76.scene")
        if (scene == null) {
            scene = if (settings.isDeveloperMode) "demo-meta-menu" else "main-menu"
        }

        sceneManager.pushScene(scene)

    }

    /**
     * Renders the current screen.
     */
    internal fun render() {
        try {
            screenManager.currentScreen.render(Gdx.graphics.deltaTime)
            if (screenManager.currentScreen !is ErrorScreen) {
                oddCareRenderer?.render()
            }
        } catch (e: Exception) {
            handleError(e)
        }

        globalTimer++
    }

}
