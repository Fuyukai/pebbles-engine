/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.font

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.fasterxml.jackson.module.kotlin.readValue
import ktx.freetype.generateFont
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.SS76EngineInternalError
import tf.veriny.ss76.engine.font.FontManifest.FontEntry
import tf.veriny.ss76.engine.util.EktFiles
import tf.veriny.ss76.engine.util.NAMED_COLOURS
import kotlin.io.path.reader

private const val CHARACTERS = """☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼!"#${'$'}%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒáíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αßΓπΣσµτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■"""

/**
 * Manages fonts and generating them.
 */
public class FontManager(private val state: EngineState) {
    private val fonts = mutableMapOf<String, Font>()

    // convenience properties
    public val defaultFont: Font get() = fonts["default"]!!
    public val errorFont: Font get() = fonts["error"] ?: defaultFont

    private fun getFontManifest(): FontManifest {
        var path = EktFiles.RESOLVER.getPath("engine/font-manifest.yaml")
        if (path == null) {
            println("No font manifest found, falling back to bundled manifest...")
            path = EktFiles.RESOLVER.getPath("engine/font-manifest-default.yaml")
            if (path == null) {
                throw SS76EngineInternalError("couldn't find font manifest file")
            }
        }

        val loaded = path.reader(Charsets.UTF_8).use {
            state.yamlLoader.readValue<FontManifest>(it)
        }
        return loaded
    }

    private fun generateFont(name: String, entry: FontEntry): Font {
        println("Fonts: Generating $name (${entry.path}, ${entry.size}, default: ${entry.defaultColour})")
        val default = NAMED_COLOURS[entry.defaultColour]
            ?: error("no such colour: ${entry.defaultColour}")

        val generator = FreeTypeFontGenerator(Gdx.files.internal(entry.path))

        val font = generator.generateFont {
            size = entry.size
            mono = true
            characters = CHARACTERS
            color = Color.WHITE
        }

        val labelFont = if (entry.generateLabelStyle) {
            generator.generateFont {
                size = entry.size
                mono = true
                characters = CHARACTERS
                color = Color.WHITE
            }
        } else null

        generator.dispose()
        return Font(name, font, labelFont, default)
    }

    /**
     * Generates all fonts in the font manifest.
     */
    public fun generateAllFonts() {
        val manifest = getFontManifest()
        for ((name, entry) in manifest.fonts) {
            val generated = generateFont(name, entry)
            fonts[name] = generated
        }
    }

    /** Gets a list of all installed fonts. */
    public fun getAllFonts(): List<Font> {
        return fonts.values.toList()
    }

    /** Gets a single font, or null if it doesn't exist. */
    public fun getFontOrNull(name: String): Font? = fonts[name]

    /** Gets a single font, throwing an error if it doesn't exist. */
    public fun getFont(name: String): Font = getFontOrNull(name) ?: error("No such font '$name'")
}
