/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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