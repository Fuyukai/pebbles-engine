/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import ktx.app.KtxInputAdapter
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.nvl.NVLRenderer
import tf.veriny.ss76.engine.system.CheckpointScene

public typealias ButtonAction = (NVLRenderer) -> Unit

/**
 * Manages the clickable buttons on a scene.
 */
public class VnButtonManager(
    private val state: EngineState,
    private val camera: OrthographicCamera,
) : KtxInputAdapter {
    public companion object {
        public val GLOBAL_BACK_BUTTON: Button = object : Button {
            override val name: String = "back-button-global"

            override fun run(state: EngineState) {
                if (state.sceneManager.stackSize > 1) {
                    state.sceneManager.exitScene()
                }
            }
        }

        public val CHECKPOINT_BUTTON: Button = object : Button {
            override val name: String = "checkpoint-button-global"
            override val linkedId: String = CheckpointScene.CHECKPOINT_SCENE_NAME

            override fun run(state: EngineState) {
                if (state.sceneManager.currentSceneIs(linkedId)) {
                    state.sceneManager.exitScene()
                } else {
                    state.sceneManager.pushScene(linkedId)
                }
            }
        }
    }

    private val rectPool = object : Pool<Rectangle>(16) {
        override fun newObject(): Rectangle {
            return Rectangle()
        }
    }

    public enum class ButtonType {
        PUSH,
        CHANGE,
        OTHER,
    }

    // mapping of button -> rectangle of possible locations on screen.
    // used when moving the mouse or clicking it
    public val buttonRects: MutableMap<Button, MutableSet<Rectangle>> = mutableMapOf()

    /**
     * Gets a rectangle ready to be filled in and added.
     */
    public fun getRectangle(): Rectangle {
        return rectPool.obtain()
    }

    /**
     * Resets the current set of button rectangles.
     */
    public fun reset() {
        for (set in buttonRects.values) {
            for (rect in set) {
                rectPool.free(rect)
            }
        }

        buttonRects.clear()
    }

    /**
     * Adds a new clickable area mapped to a button.
     */
    public fun addClickableArea(button: Button, rect: Rectangle) {
        var set = buttonRects[button]
        if (set == null) {
            set = mutableSetOf()
            buttonRects[button] = set
        }
        set.add(rect)
    }


    private fun hit(screenX: Int, screenY: Int): Button? {
        val coords = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
        camera.unproject(coords)

        for ((node, set) in buttonRects.entries) {
            for (rect in set) {
                if (rect.contains(coords.x, coords.y)) {
                    return node
                }
            }
        }

        return null
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        // always intercept this

        if (hit(screenX, screenY) != null) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand)
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        }
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val node = hit(screenX, screenY)
        if (node == null) {
            return false
        } else {
            node.run(state)
        }

        return true
    }
}
