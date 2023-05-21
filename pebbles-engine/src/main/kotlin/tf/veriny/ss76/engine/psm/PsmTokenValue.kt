/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

/**
 * Enumeration type of possible tokens.
 */
public sealed interface PsmTokenValue {
    public val text: String

    /** The single '$' token. */
    public data object Dollar : PsmTokenValue {
        override val text: String get() = "$"
    }

    /** The single '#' token. */
    public data object Sharp : PsmTokenValue {
        override val text: String get() = "#"
    }

    /** The single '[' token. */
    public data object LeftBracket : PsmTokenValue {
        override val text: String get() = "["
    }

    /** The single ']' token. */
    public data object RightBracket : PsmTokenValue {
        override val text: String get() = "]"
    }

    // used for linger
    /** The single '.' token. */
    public data object FullStop : PsmTokenValue {
        override val text: String get() = "."
    }

    // also used for linger
    /** The single ',' token. */
    public data object Comma : PsmTokenValue  {
        override val text: String get() = ","
    }

    /** A single collapsed space. */
    public data object Space : PsmTokenValue {
        override val text: String get() = " "
    }

    /** The single '=' token. */
    public data object Equals : PsmTokenValue {
        override val text: String get() = "="
    }

    /** A single newline. */
    public data object Newline : PsmTokenValue {
        override val text: String get() = "\n"
    }


    /** A special token indicating the end-of-scene. */
    public data object EndOfScene : PsmTokenValue {
        override val text: String get() = ""
    }

    /** A generic text token. */
    public data class Text(public override val text: String) : PsmTokenValue
}

public fun PsmTokenValue.isWhitespace(): Boolean = this == PsmTokenValue.Space || this == PsmTokenValue.Newline