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
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle

/**
 * The cyclable list of fonts.
 */
public class FontList(fonts: Collection<Font>) {
    private val fonts = ArrayDeque(fonts)

    public fun getCurrent(): Font {
        return fonts.first()
    }

    public fun cycle() {
        fonts.addLast(fonts.removeFirst())
    }
}

/**
 * A single generated monospace font.
 */
public class Font(
    public val name: String,
    private val normalBitmap: BitmapFont,
    private val labelBitmap: BitmapFont?,
    public val defaultColour: Color
) {

    /** The width of individual characters. Only makes sense for monospace fonts. */
    public val characterWidth: Float

    /** The height of individual characters. Only makes sense for monospace fonts. */
    public val characterHeight: Float

    private val layout = GlyphLayout(normalBitmap, " ")

    init {
        characterWidth = layout.width
        characterHeight = layout.height
        normalBitmap.color = defaultColour
    }

    /** Gets the width of the specified text. */
    public fun widthOf(text: String): Float {
        layout.setText(normalBitmap, text)
        return layout.width
    }

    /** Gets the height of the speciified text. */
    public fun heightOf(text: String): Float {
        layout.setText(normalBitmap, text)
        return layout.height
    }


    // need to separate out the fonts because scene2d fucks with the font properties sometimes.
    /**
     * Creates a new label style from this font, if it was generated with a label font.
     */
    public fun makeLabelStyle(): LabelStyle {
        check(labelBitmap != null) { "Font was not generated with a label style" }
        return LabelStyle(labelBitmap, defaultColour)
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
        normalBitmap.color = color
        layout.setText(normalBitmap, text)
        normalBitmap.draw(batch, text, x, y)
        normalBitmap.color = defaultColour
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
        layout.setText(normalBitmap, text)
        normalBitmap.draw(batch, text, x, y)
    }

}
