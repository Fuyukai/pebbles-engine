/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.EngineState
import kotlin.math.max
import kotlin.math.min

/**
 * The state for the currently running scene in NVL/ADV mode.
 */
public class SceneState(
    /** The engine state. */
    public val engineState: EngineState,
    /** The definition for this scene. */
    public val definition: VirtualNovelSceneDefinition,
    /** The current timer for this scene. */
    public var timer: Int = 0,
    /** The starting page index. */
    pageIdx: Int = 0,
) {
    private var retrievedTokens = false
    private var tokens: List<TextualNode> = emptyList()

    public var pageIdx: Int = -1
        public set(value) {
            if (field != value) {
                timer = 0
                tokens = emptyList()
                retrievedTokens = false
            }

            field = value
        }

    init {
        this.pageIdx = pageIdx
    }

    public fun canTextSkip(): Boolean {
        if (definition.modifiers.alwaysAllowTextSkip) return true

        val flag = definition.modifiers.enableTextSkipEventFlag
        val value = definition.modifiers.enableTextSkipFlagValue
        if (flag != null) {
            val existing = engineState.eventFlagsManager.getValue(flag)
            return existing == value
        }

        if (definition.modifiers.enableTextSkipOnSeen) {
            return engineState.sceneManager.hasSeenScene(definition.sceneId)
        }

        return false
    }

    /**
     * Gets the textual nodes for the current page.
     */
    public fun currentPageText(): List<TextualNode> {
        if (!retrievedTokens) {
            tokens = definition.getTokensForPage(pageIdx)
            retrievedTokens = true
        }

        return tokens
    }

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
        engineState.sceneManager.swapScene(sceneName)
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