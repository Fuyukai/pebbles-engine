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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.math.Rectangle
import ktx.app.KtxInputAdapter
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.nvl.NVLRenderer

public typealias ButtonAction = (NVLRenderer) -> Unit

/**
 * Manages the clickable buttons on a scene.
 */
public class ButtonManager(private val state: EngineState) : KtxInputAdapter {
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
            override val linkedId: String = "save-menu"

            override fun run(state: EngineState) {
                if (state.sceneManager.currentSceneIs("save-menu")) {
                    state.sceneManager.exitScene()
                } else {
                    state.sceneManager.pushScene("save-menu")
                }
            }
        }

        public val INVENTORY_BUTTON: Button = object : Button {
            override val name: String = "inventory-button-global"
            override val linkedId: String = "inventory-screen"

            override fun run(state: EngineState) {
                if (state.sceneManager.currentSceneIs("inventory-screen")) {
                    state.sceneManager.exitScene()
                } else {
                    state.sceneManager.pushScene("inventory-screen")
                }
            }
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

    public fun addClickableArea(button: Button, rect: Rectangle) {
        var set = buttonRects[button]
        if (set == null) {
            set = mutableSetOf()
            buttonRects[button] = set
        }
        set.add(rect)
    }

    /**
     * Resets the current set of button rectangles.
     */
    public fun reset() {
        buttonRects.clear()
    }

    private fun hit(screenX: Int, screenY: Int): Button? {
        val realX: Float = screenX.toFloat()
        val realY: Float = (Gdx.graphics.height - screenY.toFloat())

        for ((node, set) in buttonRects.entries) {
            for (rect in set) {
                if (rect.contains(realX, realY)) {
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