/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import com.badlogic.gdx.graphics.Color

/**
 * Container class defining the effects a scene should have.
 */
public data class SceneEffects(
    /** The custom background used for this scene. */
    public var backgroundColour: Color? = null,
    /** If this scene should be drawn inverted. */
    public var invert: Boolean = false,
    /** The custom top text used for this scene. */
    public var topText: String? = null,

    /** Lightning effect. */
    public var lightning: Boolean = false,

    /** If text skip should be disabled. */
    public var disableTextSkip: Boolean = false,

    /** If clickables should be disabled. */
    public var disableClickables: Boolean = false,
) {
    public companion object {
        public val NONE: SceneEffects = SceneEffects()
    }
}
