/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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