/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

import com.badlogic.gdx.graphics.Color
import java.util.*

public val NAMED_COLOURS: Map<String, Color> = mutableMapOf(
    "white" to Color.WHITE,
    "red" to Color.RED,
    "pink" to Color.PINK,    // 2
    "purple" to Color.PURPLE,  // 3
    "green" to Color.GREEN,   // 4
    "lime" to Color.LIME,    // 5
    "teal" to Color.TEAL,    // 6
    "blue" to Color.BLUE,    // 7
    "cyan" to Color.CYAN,
    "sky" to Color.SKY,     // 8
    "slate" to Color.SLATE,   // 9
    "orange" to Color.ORANGE,  // a
    "salmon" to Color.SALMON,   // b
    "magenta" to Color.MAGENTA, // c
    "violet" to Color.VIOLET,  // d
    "yellow" to Color.YELLOW,  // e
    "black" to Color.BLACK,   // f
)

public fun main() {
    for (key in NAMED_COLOURS.keys) {
        val caps = key.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        println("#[@=$key] $caps $[nl=1]")
    }
}
