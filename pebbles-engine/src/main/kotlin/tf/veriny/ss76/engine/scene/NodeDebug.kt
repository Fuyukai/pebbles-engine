/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

/**
 * Prints debug information about a series of textual nodes.
 */
public fun Collection<TextualNode>.debugPrintTimings() {
    for (node in this) {
        val text = when {
            node.causesNewline -> "<CRLF>"
            node.text.isBlank() -> "<empty>"
            else -> node.text
        }
        print("|${node.startFrame}|${node.endFrame}|$text")
        if (node.causesNewline) println()
        else if (node.causesSpace) print(" ")
    }
}