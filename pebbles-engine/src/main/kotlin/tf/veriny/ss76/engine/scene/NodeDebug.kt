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
        var text = when {
            node.text.isBlank() -> "<empty>"
            else -> node.text
        }
        if (node.causesNewline) text = "$text<CRLF>"

        print("|${node.startFrame}|${node.endFrame}|$text")
        if (node.causesNewline) println()
        else if (node.causesSpace) print(" ")
    }
}
