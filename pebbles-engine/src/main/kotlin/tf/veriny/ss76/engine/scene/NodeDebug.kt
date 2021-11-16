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

/**
 * Prints debug information about a series of textual nodes.
 */
public fun Collection<TextualNode>.debugPrintTimings() {
    for (node in this) {
        val text = when {
            node.causesNewline -> "<CRLF>"
            node.text.isBlank() -> "<emtpy>"
            else -> node.text
        }
        print("|${node.startFrame}|${node.endFrame}|$text")
        if (node.causesNewline) println()
        else if (node.causesSpace) print(" ")
    }
}