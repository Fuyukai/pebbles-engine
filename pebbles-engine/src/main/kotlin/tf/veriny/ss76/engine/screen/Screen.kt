/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.utils.Disposable
import ktx.app.KtxInputAdapter

/**
 * A screen is responsible for managing sub-renderers.
 */
public interface Screen : KtxInputAdapter, Disposable {
    /**
     * Renders the current screen.
     */
    public fun render(delta: Float)

    /**
     * Called when the screen is deactivated, but not disposed (e.g. is fading out). This can be
     * used to disable input. This will always be called *before* dispose.
     */
    public fun deactivated() {}
}
