/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import ktx.assets.dispose
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.screen.DummyScreen
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.screen.FadeInScreen
import tf.veriny.ss76.engine.screen.Screen

/**
 * Handles switching out SS76 screens.
 */
public class ScreenManager(internal val state: EngineState) {
    public companion object {
        public val SKIP_FADE_INS: Boolean = System.getProperty("ss76.skip-fadein", "false").toBooleanStrict()
    }

    /** The current screen being rendered. */
    public var currentScreen: Screen = DummyScreen

    /** The fade-in state, set by the fade-in screen. */
    public var fadeInState: FadeInScreen.FadeInState = FadeInScreen.FadeInState.STILL

    /** Changes to the error screen. */
    public fun error(e: Throwable) {
        val oldScreen = this.currentScreen
        oldScreen.dispose { e.addSuppressed(it) }

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.NotAllowed)

        currentScreen = ErrorScreen(state, e)
    }

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
        state.input.removeProcessor(oldScreen)

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)

        if (dispose) {
            oldScreen.dispose()
        }

        currentScreen = screen
        state.input.addProcessor(currentScreen)
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
