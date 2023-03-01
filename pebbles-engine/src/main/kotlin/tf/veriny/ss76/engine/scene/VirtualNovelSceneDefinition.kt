/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.adv.ADVSubRenderer

/**
 * Defines a single virtual novel scene.
 */
public interface VirtualNovelSceneDefinition {
    /** The mapping of buttons for this scene. */
    public val buttons: Map<String, Button>

    /** The ID of this scene. */
    public val sceneId: String

    /** The number of pages this scene has. */
    public val pageCount: Int

    /** The scene modifiers for this scene. */
    public val modifiers: SceneModifiers

    /**
     * Creates the ADV-mode renderer for this scene. If this is null, then the scene will use NVL
     * mode instead.
     */
    public fun createAdvRenderer(): ADVSubRenderer?

    /**
     * Gets the tokens for a single page. This is called ONCE when the page switches.
     */
    public fun getTokensForPage(page: Int): List<TextualNode>

    // events
    /**
     * Called when this scene is loaded.
     */
    public fun onLoad(state: SceneState)
}