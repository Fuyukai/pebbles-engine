/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.font

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import tf.veriny.ss76.engine.util.NAMED_COLOURS

/**
 * Wraps the data of a single font manifest.
 */
public data class FontManifest(
    @JsonDeserialize(keyAs = String::class, contentAs = FontEntry::class)
    public val fonts: Map<String, FontEntry>
) {
    public data class FontEntry(
        /** The path on the classpath to the font. */
        public val path: String,
        /** The size to generate the font as. */
        public val size: Int,
        /** The default colour of the font. */
        public val defaultColour: String = "white",
        /** If true, a second font instance will be generated for usage with Scene2D. */
        public val generateLabelStyle: Boolean = false,
    ) {
        init {
            check(defaultColour in NAMED_COLOURS) { "$defaultColour is not going to be generated" }
        }
    }
}