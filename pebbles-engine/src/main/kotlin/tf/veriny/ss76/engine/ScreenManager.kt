/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.nvl.NVLScreen
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.screen.Screen

/**
 * Manages SS76 screens.
 */
public class ScreenManager(private val state: EngineState) {
    /** The single instance of the NVL screen. */
    public val nvlScreenSingleton: Screen = NVLScreen(state)

    /** The current screen being rendered. */
    public lateinit var currentScreen: Screen
        private set

    /**
     * Changes to the error screen.
     */
    public fun error(e: Throwable) {
        changeScreen(ErrorScreen(state, e))
    }

    /**
     * Changes to the NVL screen.
     */
    public fun setNvlScreen() {
        changeScreen(nvlScreenSingleton)
    }

    /**
     * Changes the current screen.
     */
    public fun changeScreen(screen: Screen) {
        if (this::currentScreen.isInitialized) {
            val oldScreen = this.currentScreen
            state.input.removeProcessor(oldScreen)
            oldScreen.dispose()
        }

        currentScreen = screen
        state.input.addProcessor(currentScreen)
    }
}