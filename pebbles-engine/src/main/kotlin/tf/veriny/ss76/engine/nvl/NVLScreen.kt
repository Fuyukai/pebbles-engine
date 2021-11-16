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

package tf.veriny.ss76.engine.nvl

import com.badlogic.gdx.Input
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.ChangeSceneButton
import tf.veriny.ss76.engine.screen.Screen

/**
 * The NVL screen is responsible for rendering a scene in NVL mode.
 */
public class NVLScreen(private val state: EngineState) : Screen {
    private val currentRenderer = NVLRenderer(state)

    /**
     * Renders the current screen.
     */
    override fun render(delta: Float) {
        currentRenderer.render(state.sceneManager.currentScene)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val current = state.sceneManager.currentScene
        if (!current.definition.effects.disableTextSkip) current.timer = 999999
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        val current = state.sceneManager.currentScene
        val pagination = current.definition.enablePagination
        val textskip = !current.definition.effects.disableTextSkip

        if (pagination && (keycode == Input.Keys.LEFT || keycode == Input.Keys.DPAD_LEFT)) {
            current.pageBack()
        } else if (pagination && (keycode == Input.Keys.RIGHT || keycode == Input.Keys.DPAD_RIGHT)) {
            current.pageNext()
        } else if (textskip && keycode == Input.Keys.SPACE) {
            if (!current.definition.effects.disableTextSkip) current.timer = 999999
            return true
        } else if (keycode == Input.Keys.ENTER) {
            val buttons = state.buttonManager.buttonRects.keys.filterIsInstance<ChangeSceneButton>()

            if (buttons.isEmpty() || buttons.size > 1) return false

            val button = buttons.first()
            button.run(state)
        }

        return false
    }

    override fun dispose() {

    }
}