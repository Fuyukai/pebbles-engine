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
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.clearScreen
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.use

/**
 * The screen for rendering errors.
 */
public class ErrorScreen(
    public val state: EngineState,
    public val error: Throwable,
) : Screen {
    private var hasPrinted = false

    private val batch = SpriteBatch()

    override fun render(delta: Float) {
        clearScreen(255f, 0f, 0f, 0f)

        val tb = error.stackTraceToString()
        if (!hasPrinted) {
            error.printStackTrace()
            hasPrinted = true
        }

        batch.use {
            val message = if (state.sceneManager.stackSize == 0) {
                "Fatal error when loading engine!"
            } else {
                "Fatal error when rendering scene ${state.sceneManager.currentScene.definition.id}"
            }

            state.fontManager.errorFont.draw(
                this, message,
                1f,
                Gdx.graphics.height - 10f
            )

            state.fontManager.errorFont.draw(this, tb, 1f, Gdx.graphics.height - 30f)
        }
    }

    override fun dispose() {

    }
}