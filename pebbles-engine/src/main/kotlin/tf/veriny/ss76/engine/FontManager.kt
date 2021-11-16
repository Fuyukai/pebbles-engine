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

package tf.veriny.ss76.engine

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ktx.freetype.generateFont
import tf.veriny.ss76.SS76

/**
 * Deals with loading and generating fonts.
 */
public class FontManager {
    public companion object {
        private const val FONT_SIZE_BIG = 32
        private const val FONT_SIZE_SMALL = 20

        // auto generated colours
        public val COLOURS: Map<String, Color> = mutableMapOf(
            "white" to Color.WHITE,
            "red" to Color.RED,
            "pink" to Color.PINK,    // 2
            "purple" to Color.PURPLE,  // 3
            "green" to Color.GREEN,   // 4
            "lime" to Color.LIME,    // 5
            "teal" to Color.TEAL,    // 6
            "blue" to Color.BLUE,    // 7
            "sky" to Color.SKY,     // 8
            "slate" to Color.SLATE,   // 9
            "orange" to Color.ORANGE,  // a
            "salmon" to Color.SALMON,   // b
            "magenta" to Color.MAGENTA, // c
            "violet" to Color.VIOLET,  // d
            "yellow" to Color.YELLOW,  // e
            "black" to Color.BLACK,   // f
        )

        /** The mapping of available font name mappings. */
        public val FONT_NAME_MAPPING: MutableMap<String, String> = mutableMapOf(
            "default" to "Mx437_IBM_Model3x_Alt4.ttf",
            "alt" to "Mx437_PhoenixEGA_8x8-2y.ttf"
        )
    }

    /**
     * Wrapper class for fonts.
     */
    public class Font(
        public val fonts: MutableMap<Color, BitmapFont>
    ) {
        public val white: BitmapFont get() = fonts[Color.WHITE]!!

        /** The width of one character in this font. */
        public val characterWidth: Float = run {
            val layout = GlyphLayout()
            layout.setText(white, " ")
            layout.width
        }

        /** The height of one character in this font. */
        public val characterHeight: Float get() = white.lineHeight
    }

    /**
     * The mapping of font names to Font wrapper
     */
    public val fonts: MutableMap<String, Font> = mutableMapOf<String, Font>()

    /** The font used for the top text. */
    public lateinit var topTextFont: BitmapFont
        private set

    /** The font used for error messages. */
    public lateinit var errorFont: BitmapFont
        private set

    /** The 'default' font, useful for UI. */
    public val defaultFont: Font
        get() = fonts["default"]!!

    private fun generateFont(path: String): MutableMap<Color, BitmapFont> {
        println("generating $path")
        val generator = FreeTypeFontGenerator(Gdx.files.internal(path))
        val fonts = mutableMapOf<Color, BitmapFont>()
        for (colour in COLOURS) {
            val font = generator.generateFont {
                size = if (SS76.isBabyScreen) FONT_SIZE_SMALL else FONT_SIZE_BIG
                color = colour.value
                mono = true
                genMipMaps = true
                characters = """☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼!"#${'$'}%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒáíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αßΓπΣσµτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■"""
            }
            fonts[colour.value] = font
        }


        generator.dispose()
        return fonts
    }

    /**
     * Loads all the fonts packed with the current jar.
     */
    public fun loadAllFonts() {
        // load top text
        val topGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/Mx437_Wang_Pro_Mono.ttf"))
        topTextFont = topGenerator.generateFont {
            size = if (SS76.isBabyScreen) {
                32
            } else {
                48
            }
            mono = true
            color = Color.ORANGE
        }
        errorFont = topGenerator.generateFont {
            size = 24
            mono = true
            color = Color.WHITE
        }

        topGenerator.dispose()

        for ((name, fontName) in FONT_NAME_MAPPING) {
            val font = generateFont("fonts/$fontName")
            fonts[name] = Font(font)
        }

    }
}