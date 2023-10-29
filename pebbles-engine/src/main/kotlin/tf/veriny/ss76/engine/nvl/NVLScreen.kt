/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.nvl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.StretchViewport
import ktx.app.KtxInputAdapter
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.ChangeSceneButton
import tf.veriny.ss76.engine.VnButtonManager
import tf.veriny.ss76.engine.scene.SceneState
import tf.veriny.ss76.engine.screen.Screen

/**
 * The NVL screen is responsible for rendering a scene in NVL mode.
 */
public class NVLScreen(
    private val state: EngineState,
    private val scene: SceneState,
) : Screen, KtxInputAdapter {


    private val camera = OrthographicCamera()
    private val viewport = StretchViewport(1280f, 960f, camera)
    private val buttons = VnButtonManager(state, camera)
    private val currentRenderer: NVLRendererV3

    init {
        println("creating renderer for ${scene.definition.sceneId}")
        viewport.update(
            /* screenWidth = */ Gdx.graphics.width,
            /* screenHeight = */ Gdx.graphics.height,
            /* centerCamera = */ true
        )

        camera.update()

        currentRenderer = NVLRendererV3(scene, viewport, camera, buttons)
    }

    /**
     * Renders the current screen.
     */
    override fun render(delta: Float) {
        currentRenderer.render()
        scene.timer++
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return buttons.mouseMoved(screenX, screenY)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (buttons.touchDown(screenX, screenY, pointer, button)) return true
        if (scene.timer >= 0 && scene.canTextSkip()) {
            scene.timer = 999999
            return true
        }

        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        val current = scene
        val pagination = current.definition.modifiers.drawPageButtons

        if (pagination && (keycode == Input.Keys.LEFT)) {
            current.pageBack()
        } else if (pagination && (keycode == Input.Keys.RIGHT)) {
            current.pageNext()
        } else if (scene.canTextSkip() && keycode == Input.Keys.SPACE) {
            current.timer = 999999
            return true
        } else if (keycode == Input.Keys.ENTER) {
            val buttons = buttons.buttonRects.keys.filterIsInstance<ChangeSceneButton>()

            if (buttons.isEmpty() || buttons.size > 1) return false

            val button = buttons.first()
            button.run(state)
        } else if (keycode == Input.Keys.BACKSPACE) {
            if (state.sceneManager.stackSize > 1) state.sceneManager.exitScene()
        }

        return false
    }

    override fun dispose() {
        currentRenderer.dispose()
    }
}
