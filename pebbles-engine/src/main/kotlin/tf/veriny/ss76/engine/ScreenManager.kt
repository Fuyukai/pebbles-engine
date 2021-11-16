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