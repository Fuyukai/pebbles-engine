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

package tf.veriny.ss76.engine.adv

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import ktx.app.clearScreen
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.ChangeSceneButton
import tf.veriny.ss76.engine.screen.Screen
import tf.veriny.ss76.use

/**
 * Screen for displaying ADV mode.
 */
public class ADVScreen(
    public val state: EngineState,
    public val subrenderer: ADVSubRenderer,
) : Screen {
    public val renderer: ADVRenderer = ADVRenderer(subrenderer, state)

    init {
        subrenderer.create()
    }

    public fun isAlreadyRendering(renderer: ADVSubRenderer): Boolean = this.renderer.subRenderer == renderer

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)

        renderer.render(state.sceneManager.currentScene)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // todo: maybe dont succ input

        val current = state.sceneManager.currentScene
        val nodes = current.definition.getTokensForPage(current.pageIdx)
        val frame = nodes.last().endFrame
        if (current.timer > frame) {
            current.pageNext()
        } else {
            current.timer = 999999999
        }

        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        val current = state.sceneManager.currentScene

        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.DPAD_LEFT) {
            current.pageBack()
            return true
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.DPAD_RIGHT) {
            current.pageNext()
            return true
        } else if (keycode == Input.Keys.SPACE) {
            current.timer = 99999999
            return true
        }

        return subrenderer.keyDown(keycode)
    }

    override fun dispose() {
        subrenderer.dispose()
    }
}