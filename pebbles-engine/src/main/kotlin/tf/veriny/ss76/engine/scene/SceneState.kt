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

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.EngineState
import kotlin.math.max
import kotlin.math.min

/**
 * Shared state for a single scene.
 */
public class SceneState(
    /** The engine state. */
    public val engineState: EngineState,
    /** The definition for this scene. */
    public val definition: SceneDefinition,
    /** The current timer for this scene. */
    public var timer: Int = 0,
    /** The current page index. */
    public var pageIdx: Int = 0,
) {

    /** Jumps to the next page, clamped. */
    public fun pageNext() {
        val oldIdx = pageIdx
        pageIdx = min(pageIdx + 1, definition.pageCount - 1)
        if (oldIdx != pageIdx) timer = 0
    }

    /** Jumps to the previous page, clamped. */
    public fun pageBack() {
        val oldIdx = pageIdx
        pageIdx = max(0, pageIdx - 1)
        if (oldIdx != pageIdx) timer = 0
    }

    /**
     * Changes the current scene.
     */
    public fun changeScene(sceneName: String) {
        engineState.sceneManager.changeScene(sceneName)
    }

    /** Pushes a new scene onto the scene stack. */
    public fun pushScene(sceneName: String) {
        engineState.sceneManager.pushScene(sceneName)
    }

    /** Exits the current scene. */
    public fun exitScene() {
        engineState.sceneManager.exitScene()
    }
}