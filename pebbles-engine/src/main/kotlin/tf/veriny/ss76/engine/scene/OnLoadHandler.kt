/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

/**
 * Helper interface for when a scene is loaded.
 */
public fun interface OnLoadHandler {
    /**
     * Called immediately before a scene is loaded. Can be used to do things like play music,
     * swap pages, etc.
     */
    public fun onLoad(state: SceneState)
}