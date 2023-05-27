/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import tf.veriny.ss76.EngineState

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
            state.handleError(e)
            false
        }
    }

    override fun keyTyped(character: Char): Boolean {
        return try {
            super.keyTyped(character)
        } catch (e: Exception) {
            state.handleError(e)
            false
        }
    }

    override fun keyUp(keycode: Int): Boolean {
        return try {
            super.keyUp(keycode)
        } catch (e: Exception) {
            state.handleError(e)
            false
        }
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return try {
            super.mouseMoved(screenX, screenY)
        } catch (e: Exception) {
            state.handleError(e)
            false
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return try {
            super.touchDown(screenX, screenY, pointer, button)
        } catch (e: Exception) {
            state.handleError(e)
            false
        }
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return try {
            super.touchDragged(screenX, screenY, pointer)
        } catch (e: Exception) {
            state.handleError(e)
            false
        }
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return try {
            super.touchUp(screenX, screenY, pointer, button)
        } catch (e: Exception) {
            state.handleError(e)
            false
        }
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return try {
            super.scrolled(amountX, amountY)
        } catch (e: Exception) {
            state.handleError(e)
            false
        }
    }


}