/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.engine.*
import tf.veriny.ss76.engine.adv.ADVSubRenderer

public typealias onLoad = (SceneState) -> Unit


/**
 * A scene definition that has already been prepared. This is useful for non-dynamic scenes.
 */
public class SceneDefinition(
    /** The ID of this scene. */
    public val id: String,
    /** The original buttons for this scene. */
    public val originalButtons: Map<String, Button>,
    /** The pages of content for this scene. */
    private val pages: List<List<TextualNode>>,
    /** The original (unparsed) content for this scene. */
    public val originalPages: List<String>,
    /** The effects this scene should use. */
    public val effects: SceneEffects,
    /** The linked inventory ID, if any. */
    public val linkedInventoryId: Int = -1,
    /** If this scene is marked as dynamic. */
    public val dynamic: Boolean = false,
    /** If this scene has an ADV renderer. */  // doesn't work well with save/loading
    public val advSubRenderer: ADVSubRenderer? = null,
    /** The functions to run when this scene loads. */
    public val onLoadHandlers: List<onLoad> = listOf(),
    /** If pagination should be enabled. */
    public val enablePagination: Boolean = true,
)  {
    public val buttons: Map<String, Button> = originalButtons.toMutableMap().apply {
        put("page-next", NextPageButton)
        put("page-back", PrevPageButton)

        put("record", ButtonManager.GLOBAL_BACK_BUTTON)
        put("checkpoint", ButtonManager.CHECKPOINT_BUTTON)
    }

    /** If this definition has custom onLoad handlers. */
    public val hasCustomOnLoad: Boolean = onLoadHandlers.isNotEmpty()

    /** Gets the page count for the specified page. */
    public val pageCount: Int get() = pages.size

    /**
     * Gets the tokens for the specified page.
     */
    public fun getTokensForPage(pageIdx: Int): List<TextualNode> {
        return pages[pageIdx]
    }
}


