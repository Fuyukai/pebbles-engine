/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.font

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * A single generated monospace font.
 */
public class Font(
    public val name: String,
    private val bitmap: BitmapFont,
    public val defaultColour: Color
) {

    /** The width of individual characters. Only makes sense for monospace fonts. */
    public val characterWidth: Float

    /** The height of individual characters. Only makes sense for monospace fonts. */
    public val characterHeight: Float

    private val layout = GlyphLayout(bitmap, " ")

    init {
        characterWidth = layout.width
        characterHeight = layout.height
        bitmap.color = defaultColour
    }

    /** Gets the width of the specified text. */
    public fun widthOf(text: String): Float {
        layout.setText(bitmap, text)
        return layout.width
    }

    /** Gets the height of the speciified text. */
    public fun heightOf(text: String): Float {
        layout.setText(bitmap, text)
        return layout.height
    }

    /**
     * Draws [text] (coloured as [color]) using this font at ([x], [y]).
     */
    public fun drawWithColour(
        batch: SpriteBatch,
        text: String,
        color: Color,
        x: Float,
        y: Float
    ) {
        bitmap.color = color
        layout.setText(bitmap, text)
        bitmap.draw(batch, text, x, y)
        bitmap.color = defaultColour
    }

    /**
     * Draws [text] (coloured with the default colour) using this font at ([x], [y]).
     */
    public fun draw(
        batch: SpriteBatch,
        text: String,
        x: Float,
        y: Float
    ) {
        // we can avoid any calls to setColor here as drawWithColour automatically sets it back
        layout.setText(bitmap, text)
        bitmap.draw(batch, text, x, y)
    }

}