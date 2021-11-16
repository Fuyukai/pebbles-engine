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

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.Gdx
import ktx.app.clearScreen
import tf.veriny.ss76.SS76
import kotlin.math.max
import kotlin.math.min

/**
 * Shifts the window size each frame.
 */
public class WindowSizeShifter(
    public val newHeight: Int, public val newWidth: Int,
) : Screen {

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)

        var changed = false
        var cHeight = Gdx.graphics.height
        var cWidth = Gdx.graphics.width

        if (Gdx.graphics.height > newHeight) {
            cHeight = max(cHeight - 7, newHeight)
            changed = true
        } else if (Gdx.graphics.height < newHeight) {
            cHeight = min(cHeight + 7, newHeight)
            changed = true
        }

        if (Gdx.graphics.width > newWidth) {
            cWidth = max(cWidth - 7, newWidth)
            changed = true
        } else if (Gdx.graphics.width < newWidth) {
            cWidth = min(cWidth + 7, newWidth)
            changed = true
        }

        if (changed) {
            Gdx.graphics.setWindowedMode(cWidth, cHeight)
        } else {
            //SS76.changeScreen(nextScreen)
        }
    }

    override fun dispose() {

    }
}