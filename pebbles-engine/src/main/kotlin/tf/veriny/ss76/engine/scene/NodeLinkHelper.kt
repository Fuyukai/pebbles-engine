/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.EngineState


public class NodeLinkHelper(private val state: EngineState) {
    /**
     * Checks if the specified linked node should be marked as green (or, successful).
     */
    public fun isNodeGreen(scene: VirtualNovelSceneDefinition, node: TextualNode): Boolean {
        if (!node.colourLinkedToButton) return true

        // TODO: extend the colour linking system
        val button = scene.buttons[node.buttonId] ?: return false
        return button.linkedId?.let { state.sceneManager.hasCompletedScene(it) } ?: false
    }
}
