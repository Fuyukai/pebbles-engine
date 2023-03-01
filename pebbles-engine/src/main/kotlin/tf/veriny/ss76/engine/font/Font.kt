/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.font

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout

/**
 * A single generated font.
 */
public class Font(
    public val name: String,
    private val colours: Map<Color, BitmapFont>,
    defaultColour: Color,
) {
    /** Gets the [BitmapFont] for the specified colour. */
    public fun forColour(colour: Color): BitmapFont {
        return colours[colour]
               ?: throw IllegalArgumentException("colour $colour was not generated for font '$name'")
    }

    /** The generated [BitmapFont] in the default colour. */
    public val default: BitmapFont = colours[defaultColour]!!

    /** The width of individual characters. */
    public val characterWidth: Float

    /** The height of individual characters. */
    public val characterHeight: Float

    init {
        val layout = GlyphLayout(default, " ")
        characterWidth = layout.width
        characterHeight = layout.height
    }
}