/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.app.clearScreen
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.use

/**
 * Smoothly fades between two different screens.
 */
public class FadeInScreen(
    private val previousScreen: Screen,
    private val newScreen: Screen,
    private val state: EngineState,
    private val callback: (EngineState) -> Unit,
) : Screen {
    private companion object {
        private val BASE_COLOR = Color.BLACK
    }

    public enum class FadeInState {
        FADING_OUT,
        STILL,
        FADING_IN,
        ;
    }

    private var timer = 0f
    private val shape = ShapeRenderer()

    public var fadeInState: FadeInState = FadeInState.FADING_IN

    override fun dispose() {
        shape.dispose()
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f)

        val colour = BASE_COLOR.cpy()

        if (timer > 120f) {
            newScreen.render(delta)
            // finished rendering, swap to new screen
            state.screenManager.changeScreen(newScreen, dispose = false)
            dispose()
            fadeInState = FadeInState.STILL
            return callback(state)
        } else if (timer > 90f) {
            // white fade in
            var alpha = (120f - timer) / 30f
            if (alpha.isNaN()) {
                alpha = 1f
            }

            colour.a = alpha
            newScreen.render(delta)
            fadeInState = FadeInState.FADING_IN
        } else if (timer > 30f) {
            // stay white
            fadeInState = FadeInState.STILL
        } else {
            // white fade-out
            var alpha = timer / 30f
            if (alpha.isNaN()) {
                alpha = 1f
            }

            colour.a = alpha
            previousScreen.render(delta)
            fadeInState = FadeInState.FADING_OUT
        }

        timer++

        Gdx.gl.glEnable(GL30.GL_BLEND)
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shape.use(ShapeRenderer.ShapeType.Filled) {
            rect(
                0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(),
                colour, colour, colour, colour
            )
        }
        Gdx.gl.glDisable(GL30.GL_BLEND)

    }
}
