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
