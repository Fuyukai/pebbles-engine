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


