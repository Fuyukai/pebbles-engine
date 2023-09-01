/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

import tf.veriny.ss76.engine.scene.builder.PageIndex
import tf.veriny.ss76.engine.scene.builder.SceneBuilder
import tf.veriny.ss76.engine.scene.builder.SceneBuilder.PageBuilder

public class RawPageBuilder
@PublishedApi internal constructor(private val original: PageBuilder) {
    private val stringBuilder = StringBuilder()

    private fun ensureSpace() {
        if (stringBuilder.isEmpty()) return

        if (!stringBuilder.last().isWhitespace()) {
            stringBuilder.append(' ')
        }
    }

    /**
     * Inserts the provided number of newlines.
     */
    public fun nl(count: Int = 1) {
        when {
            count <= 0 -> {}
            count == 1 -> {
                ensureSpace()
                stringBuilder.append(PsmFragmentManager.RAW_SINGLE_NEWLINE)
            }
            else -> {
                ensureSpace()
                stringBuilder.append("$[nl=${count}]")
            }
        }
    }

    /**
     * Creates a new raw fragment with the provided literal line content.
     */
    public fun rline(content: String) {
        ensureSpace()
        stringBuilder.append(content)
    }

    /**
     * Adds a new raw line with the provided amount of newlines after it.
     */
    public fun nlline(content: String, newlines: Int = 1) {
        rline(content)
        nl(newlines)
    }

    /**
     * Adds a new change scene button with the provided content.
     */
    public fun cs(scene: String, content: String) {
        ensureSpace()
        stringBuilder.append("$[`=cs-${scene},@=salmon] $content $$")
    }

    /**
     * Adds a new push scene button with the provided content.
     */
    public fun ps(scene: String, content: String, colour: String? = null) {
        ensureSpace()
        if (colour == null) {
            stringBuilder.append("$[`=ps-${scene},%=true] $content $$")
        } else {
            stringBuilder.append("$[`=ps-${scene},@=$colour] $content $$")
        }
    }

    /**
     * Adds a new dialogue-line using the dialogue macro.
     */
    public fun dline(name: String, content: String) {
        ensureSpace()
        stringBuilder.append("%dl(${name})% $content $$")
    }

    override fun toString(): String {
        return this.stringBuilder.toString()
    }
}

/**
 * Creates a new scene using a [RawPageBuilder].
 */
public inline fun SceneBuilder.rawScene(crossinline block: RawPageBuilder.() -> Unit): PageIndex {
    return page {
        val realBuilder = RawPageBuilder(this)
        realBuilder.block()
        addRawFragment(realBuilder.toString())
    }
}
