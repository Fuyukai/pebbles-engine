/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

/**
 * Enumeration type of possible tokens.
 */
public sealed interface PsmToken {
    public val value: String

    /** The single '$' token. */
    public data object Dollar : PsmToken {
        override val value: String get() = "$"
    }

    /** The single '#' token. */
    public data object Sharp : PsmToken {
        override val value: String get() = "#"
    }

    /** The single '[' token. */
    public data object LeftBracket : PsmToken {
        override val value: String get() = "["
    }

    /** The single ']' token. */
    public data object RightBracket : PsmToken {
        override val value: String get() = "]"
    }

    // used for linger
    /** The single '.' token. */
    public data object FullStop : PsmToken {
        override val value: String get() = "."
    }

    // also used for linger
    /** The single ',' token. */
    public data object Comma : PsmToken  {
        override val value: String get() = ","
    }

    /** A single collapsed space. */
    public data object Space : PsmToken {
        override val value: String get() = " "
    }

    /** The single '=' token. */
    public data object Equals : PsmToken {
        override val value: String get() = "="
    }

    /** A single newline. */
    public data object Newline : PsmToken {
        override val value: String get() = "\n"
    }


    /** A special token indicating the end-of-scene. */
    public data object EndOfScene : PsmToken {
        override val value: String get() = ""
    }

    /** A generic text token. */
    public data class Text(public override val value: String) : PsmToken
}

public fun PsmToken.isWhitespace(): Boolean = this == PsmToken.Space || this == PsmToken.Newline