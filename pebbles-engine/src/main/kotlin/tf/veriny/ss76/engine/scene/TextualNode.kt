/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import com.badlogic.gdx.graphics.Color

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
    /** The font for this node. */
    public var fontName: String = "default",
    /** The colour of this node. If null, the font's default colour will be used./ */
    public var colour: Color? = null,
    /** If the colour for this node is linked to scene visitation state. */
    public var colourLinkedToButton: Boolean = false,
    /** The special effects for this node. */
    public var effects: MutableSet<Effect> = mutableSetOf(),
    /** The left padding (in characters) for this node. */
    public var padding: Int = 0,
    /** The associated button ID for this node. */
    public var buttonId: String? = null,
) {
    public enum class Effect {
        // (sorta) non-rendered effects
        /** 6 chars of padding per line */
        DIALOGUE,

        /** prevent frame counter increment */
        INSTANT,

        /** Resets the frame counter */
        RESET,


        // effects handled by the word drawing
        /** Causes shaking */
        SHAKE,

        // text replacement effects
        /** Shuffles with random numbers */
        SHUFNUM,

        /** Shuffles with random text */
        SHUFTXT,

        /** Mojibakes the text */
        MOJIBAKE,

        /** Causes the text to undergo a rainbow effect. */
        RAINBOWIFY,

        // unimplemented
        /** since wave effect */
        SINE,

        /** trollface */
        AYANA,
    }
}
