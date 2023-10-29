/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.NextPageButton
import tf.veriny.ss76.engine.PrevPageButton
import tf.veriny.ss76.engine.VnButtonManager
import tf.veriny.ss76.engine.adv.ADVSubRenderer

/**
 * A scene definition that lazily creates new scene data.
 */
public class SceneDefinition(
    /** The ID of this scene. */
    public override val sceneId: String,

    /** The original buttons for this scene. */
    public val originalButtons: Map<String, Button>,

    /** The pages of content for this scene. */
    private val pages: List<List<TextualNode>>,

    /** The original (unparsed) content for this scene. */
    public val originalPages: List<String>,

    /** If this scene has an ADV renderer. */  // doesn't work well with save/loading
    public val advSubRenderer: ADVSubRenderer? = null,

    /** The functions to run when this scene loads. */
    public val onLoadHandlers: List<OnLoadHandler> = listOf(),

    override val modifiers: SceneModifiers
) : VirtualNovelSceneDefinition {
    public companion object {
        public val GLOBAL_BUTTONS: Map<String, Button> = mutableMapOf(
            "page-next" to NextPageButton,
            "page-back" to PrevPageButton,
            "back-button" to VnButtonManager.CHECKPOINT_BUTTON,
            "checkpoint" to VnButtonManager.GLOBAL_BACK_BUTTON,
        )
    }

    public override val buttons: Map<String, Button> = originalButtons.toMutableMap().also {
        it.putAll(GLOBAL_BUTTONS)
    }

    /** If this definition has custom onLoad handlers. */
    public val hasCustomOnLoad: Boolean = onLoadHandlers.isNotEmpty()

    override fun onLoad(state: SceneState) {
        onLoadHandlers.forEach { it.onLoad(state) }
    }

    override fun createAdvRenderer(): ADVSubRenderer? {
        return advSubRenderer
    }

    /** Gets the page count for the specified page. */
    public override val pageCount: Int get() = pages.size

    /**
     * Gets the tokens for the specified page.
     */
    public override fun getTokensForPage(page: Int): List<TextualNode> {
        return pages[page]
    }
}
