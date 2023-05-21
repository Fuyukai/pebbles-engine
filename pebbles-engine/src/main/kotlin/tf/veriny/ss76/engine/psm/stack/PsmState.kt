/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm.stack

import com.badlogic.gdx.graphics.Color
import tf.veriny.ss76.engine.scene.TextualNode

// default state constants
private const val DEFAULT_RIGHT_MARGIN = 70
private const val DEFAULT_LEFT_MARGIN = 0
private const val DEFAULT_NEWLINE_LINGER_FRAMES = 45
private const val DEFAULT_FRAMES_PER_WORD = 5

/**
 * A stack of [PsmStateEntry] that is used when baking scenes.
 */
internal class PsmState {
    class PsmStateNode(val entries: List<PsmStateEntry<*>>)

    var temp: PsmStateNode? = null
    val stack = ArrayDeque<PsmStateNode>()
    val reversed = stack.asReversed()

    fun push(node: PsmStateNode) = stack.addLast(node)
    fun pop() = stack.removeLast()
    fun removeTemp() { temp = null }

    fun clear() {
        stack.clear()
        temp = null
    }
}

/**
 * Scans the list of stack entries from top to bottom to find the node with the type [T],
 * returning [default] if there was no such entry.
 */
internal inline fun <Value : Any, reified T : PsmStateEntry<Value>> PsmState.find(default: Value): Value {
    if (temp != null) {
        for (sentry in temp?.entries!!) {
            if (sentry is T) return sentry.value
        }
    }

    for (entry in reversed) {
        for (sentry in entry.entries) {
            if (sentry is T) {
                return sentry.value
            }
        }
    }

    return default
}

/**
 * Scans the list of stack entries from top to bottom to find the node with the type [T].
 */
internal inline fun <Value : Any, reified T : PsmStateEntry<Value>> PsmState.find(): Value? {
    if (temp != null) {
        for (sentry in temp?.entries!!) {
            if (sentry is T) return sentry.value
        }
    }

    for (entry in reversed) {
        for (sentry in entry.entries) {
            if (sentry is T) {
                return sentry.value
            }
        }
    }

    return null
}

// aliases
internal inline val PsmState.instant: Boolean
    get() = find<Boolean, PsmInstant>(false)

internal inline val PsmState.chomp: Boolean
    get() = find<Boolean, PsmChomp>(false)

internal inline val PsmState.newlineLinger: Boolean
    get() = find<Boolean, PsmNewlineLinger>(true)

internal inline val PsmState.colourButtonLink: Boolean
    get() = find<Boolean, PsmColourButtonLink>(false)

internal inline val PsmState.rightMargin: Int
    get() = find<Int, PsmRightMargin>(DEFAULT_RIGHT_MARGIN)

internal inline val PsmState.leftMargin: Int
    get() = find<Int, PsmLeftMargin>(DEFAULT_LEFT_MARGIN)

internal inline val PsmState.font: String
    get() = find<String, PsmFont>("default")

internal inline val PsmState.newlineLingerFrames: Int
    get() = find<Int, PsmNewlineLingerFrames>(DEFAULT_NEWLINE_LINGER_FRAMES)

internal inline val PsmState.lingerFrames: Int
    get() = find<Int, PsmLingerFrames>(0)

internal inline val PsmState.framesPerWord: Int
    get() = find<Int, PsmFramesPerWord>(DEFAULT_FRAMES_PER_WORD)

internal inline val PsmState.colour: Color?
    get() = find<Color, PsmColour>()

internal inline val PsmState.effect: Set<TextualNode.Effect>
    get() = find<Set<TextualNode.Effect>, PsmEffect>(emptySet())

internal inline val PsmState.button: String?
    get() = find<String, PsmButton>()