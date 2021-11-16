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

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.screen.ErrorScreen

/**
 * An input multiplexer that catches any errors automatically.
 */
public class SafeMultiplexer(
    private val state: EngineState,
    vararg processors: InputProcessor
) : InputMultiplexer(*processors) {
    override fun keyDown(keycode: Int): Boolean {
        return try {
            super.keyDown(keycode)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }

    override fun keyTyped(character: Char): Boolean {
        return try {
            super.keyTyped(character)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }

    override fun keyUp(keycode: Int): Boolean {
        return try {
            super.keyUp(keycode)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return try {
            super.mouseMoved(screenX, screenY)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return try {
            super.touchDown(screenX, screenY, pointer, button)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return try {
            super.touchDragged(screenX, screenY, pointer)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return try {
            super.touchUp(screenX, screenY, pointer, button)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return try {
            super.scrolled(amountX, amountY)
        } catch (e: Exception) {
            state.screenManager.error(e)
            false
        }
    }


}