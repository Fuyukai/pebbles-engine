/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Cursor
import ktx.assets.dispose
import tf.veriny.ss76.EngineState

/**
 * Handles switching out SS76 screens.
 */
public class ScreenManager(internal val state: EngineState) {
    public companion object {
        public val SKIP_FADE_INS: Boolean = System.getProperty("ss76.skip-fadein", "false").toBooleanStrict()
    }

    // The number of input processors to remove from the input multiplexer
    private var cachedInputProcessors: Collection<InputProcessor> = listOf()

    /** The current screen being rendered. */
    public var currentScreen: Screen = DummyScreen
        private set

    /** Changes to the error screen. */
    public fun error(e: Throwable) {
        val oldScreen = this.currentScreen
        oldScreen.dispose { e.addSuppressed(it) }

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NotAllowed)

        currentScreen = ErrorScreen(state, e)
    }

    /**
     * Fades in a new screen automatically using [FadeInScreen].
     */
    public fun fadeIn(newScreen: Screen) {
        if (SKIP_FADE_INS) {
            changeScreen(newScreen, dispose = true)
        } else {
            val old = currentScreen
            val fadeInScreen = FadeInScreen(old, newScreen, state) { old.dispose() }
            changeScreen(fadeInScreen, dispose = false)
        }
    }

    /**
     * Changes the current screen.
     */
    public fun changeScreen(screen: Screen, dispose: Boolean = true) {
        val oldScreen = this.currentScreen
        for (it in cachedInputProcessors) { state.input.removeProcessor(it) }

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)

        if (dispose) {
            oldScreen.dispose()
        }

        currentScreen = screen
        cachedInputProcessors = screen.getInputProcessors()
        for (it in cachedInputProcessors) { state.input.addProcessor(it) }
    }

    /**
     * Changes the current screen to the provided screen, returning a shim screen that can be used
     * to swap back.
     */
    public fun changeScreenWithShim(screen: Screen): ShimScreen {
        val next = ShimScreen(currentScreen, screen)
        changeScreen(next, dispose = false)
        return next
    }

    /**
     * Exits the current screen and returns to the one provided by the shim.
     */
    public fun exitScreenWithShim(shim: ShimScreen) {
        changeScreen(shim.previousScreen)
    }
}

/**
 * Changes the current screen and clears the scene manager scene stack.
 *
 * For usage with technical scenes that need to change screens.
 */
public fun ScreenManager.changeScreenAndClearScenes(screen: Screen) {
    state.sceneManager.clear()
    changeScreen(screen)
}
