/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.utils.Disposable

/**
 * A screen is responsible for managing sub-renderers.
 */
public interface Screen : Disposable {
    /**
     * Renders the current screen.
     */
    public fun render(delta: Float)

    /**
     * Called when the screen is deactivated, but not disposed (e.g. is fading out). This will
     * always be called *before* dispose.
     */
    public fun deactivated() {}

    /**
     * Gets the list of input processors for this screen.
     *
     * By default, this will return a single-item list consisting of this object if it implements
     * [InputProcessor] in some form, or an empty list if it doesn't.
     */
    public fun getInputProcessors(): Collection<InputProcessor> {
        return if (this is InputProcessor) listOf(this)
        else emptyList()
    }
}
