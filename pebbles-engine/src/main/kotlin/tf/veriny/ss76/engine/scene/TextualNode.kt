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
import tf.veriny.ss76.engine.FontManager

/**
 * A single word in a virtual novel.
 */
public data class TextualNode(
    /** The actual text of the node. */
    public val text: String,
    /** The start frame for this node. */
    public var startFrame: Int,
    /** The end frame for this node. */
    public var endFrame: Int,
    /** If this node causes a newline. */
    public var causesNewline: Boolean = false,
    /** If this node causes a space gap. */
    public var causesSpace: Boolean = true,
    /** The colour of this node. */
    public var colour: Color = Color.WHITE,
    /**
     * If this node is linked to a button. Overrides the colour with the visited/unvisited
     * colours.
     */
    public var colourLinkedToButton: Boolean = false,
    /** The special effects for this node. */
    public var effects: MutableSet<Effect> = mutableSetOf(),
    /** The left padding (in characters) for this node. */
    public var padding: Int = 0,
    /** The associated button ID for this node. */
    public var buttonId: String? = null,
    /** The font for this node. */
    public var font: String = "default",
) {
    public enum class Effect {
        /** Causes padding */
        DIALOGUE,
        /** Causes shaking */
        SHAKE,
        /** Shuffles with random numbers */
        SHUFNUM,
        /** Shuffles with random text */
        SHUFTXT,
        /** Mojibakes the text */
        MOJIBAKE,
        /** Resets the frame counter */
        RESET,
        /** since wave effect */
        SINE,
        /** trollface */
        AYANA,
    }

    /**
     * Gets the expanded representation of this node.
     */
    public fun repr(): String {
        val builder = StringBuilder()

        if (colourLinkedToButton) {
            builder.append("@linked@")
        } else if (colour != Color.WHITE) {
            val name = FontManager.COLOURS.entries.find { it.value == colour }!!.key
            builder.append('@')
            builder.append(name)
            builder.append('@')
        }

        if (effects.isNotEmpty()) {
            builder.append('¬')
            builder.append(effects.joinToString(",") { it.name })
            builder.append('¬')
        }

        if (buttonId != null) {
            builder.append('`')
            builder.append(buttonId)
            builder.append('`')
        }

        builder.append(text)

        if (causesNewline && Effect.DIALOGUE !in effects) builder.append('\n')
        if (causesSpace) builder.append(" ")

        return builder.toString()
    }
}
