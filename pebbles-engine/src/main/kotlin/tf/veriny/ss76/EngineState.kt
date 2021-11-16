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

package tf.veriny.ss76

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import ktx.app.KtxInputAdapter
import tf.veriny.ss76.engine.*
import tf.veriny.ss76.engine.renderer.OddCareRenderer
import tf.veriny.ss76.engine.scene.createAndRegisterScene
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.system.registerSystemScenes
import java.lang.management.MemoryUsage
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * The EngineState object wraps systems required to run the SS76 engine.
 */
public class EngineState(
    /** The namespace for this engine state, used mostly for save data. */
    public val namespace: String,
    /** The registration object that will register the virtual novel scenes. */
    public val registrar: SceneRegistrar,
    /** The default top text to use when rendering. */
    public val defaultTopText: String,
) {
    private val demoRenderer = OddCareRenderer()

    /** The global frame timer. Increments monotonically by one every frame. */
    public var globalTimer: Long = 0L
        private set

    /** The font manager for rendering text. */
    public val fontManager: FontManager = FontManager()

    /** The scene manager for Virtual Novel scenes. */
    public val sceneManager: SceneManager = SceneManager(
        this, namespace
    )

    /** The checkpoint manager for saving and loading VN scene progress. */
    public val checkpointManager: CheckpointManager = CheckpointManager(
        this, namespace
    )

    /** The button manager, for VN scene buttons. */
    public val buttonManager: ButtonManager = ButtonManager(this)

    /** The event flag manager. */
    public val eventFlagsManager: EventFlags = EventFlags(this)

    /** The screen manager, used to swap out the current renderer. */
    public val screenManager: ScreenManager = ScreenManager(this)

    /** The input multiplexer, used for input. */
    public val input: InputMultiplexer = SafeMultiplexer(this, object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            // helper functionality that overrides all sub-screens.
            when (keycode) {
                Input.Keys.F1 -> {
                    openDataScene()
                }

                Input.Keys.F2 -> {
                    // push demo UI
                    if (SS76.IS_DEMO) {
                        repeat(sceneManager.stackSize - 1) { sceneManager.exitScene() }
                        sceneManager.changeScene("demo-meta-menu")
                    }
                }
                Input.Keys.F3 -> {

                }
                else -> return super.keyDown(keycode)
            }

            return true
        }
    })

    private fun openDataScene() {
        val scene = sceneManager.createAndRegisterScene("data-scene") {
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

        sceneManager.pushScene(scene)
    }

    @OptIn(ExperimentalTime::class)
    internal fun created() {
        println("Loading engine state....")
        input.addProcessor(buttonManager)

        val fontGenTime = measureTime {
            fontManager.loadAllFonts()
        }
        println("All fonts generated in $fontGenTime.")

        sceneManager.loadSeenScenes()
        checkpointManager.register()

        val timeTaken = measureTime {
            registrar.register(sceneManager)

            val sceneName = if (SS76.IS_DEBUG) {
                "demo-meta-menu"
            } else {
                System.getProperty("scene", "main-menu")
            }
            registerSystemScenes(sceneName, sceneManager)
            screenManager.changeScreen(ErrorScreen(
                this,
                Exception("Initial scene was not set!")
            ))
        }
        println("Registered ${sceneManager.sceneCount} scenes in $timeTaken.")

        sceneManager.pushScene("system-startup-scene")
    }

    /**
     * Renders the current screen.
     */
    internal fun render() {
        screenManager.currentScreen.render(Gdx.graphics.deltaTime)
        if (SS76.IS_DEMO) {
            demoRenderer.render()
        }

        globalTimer++
    }

}