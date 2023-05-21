/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

import com.badlogic.gdx.graphics.Color
import tf.veriny.ss76.engine.SS76EngineInternalError
import tf.veriny.ss76.engine.psm.stack.*
import tf.veriny.ss76.engine.scene.TextualNode
import tf.veriny.ss76.engine.util.NAMED_COLOURS
import tf.veriny.ss76.toBooleanHandleBlank

/**
 * Bakes Pebbles Scene Markup into a series of [TextualNode] instances.
 */
@OptIn(ExperimentalStdlibApi::class)
public class SceneBaker {
    private companion object {

    }

    public val tokenizer: PsmTokenizer = PsmTokenizer()
    private val state = PsmState()
    private val currentNodes = mutableListOf<TextualNode>()

    private val directiveHandlers = mutableMapOf<String, (String) -> PsmStateEntry<*>?>()

    private fun addHandler(vararg keys: String, handler: (String) -> PsmStateEntry<*>?) {
        for (key in keys) {
            directiveHandlers[key] = handler
        }
    }

    init {
        addHandler("pop") {
            throw PsmSyntaxError("use '$$' for popping")
        }
        addHandler("newline-linger") {
            PsmNewlineLinger(it.toBooleanHandleBlank(true))
        }
        addHandler("newline-linger-frames") {
            PsmNewlineLingerFrames(it.toInt())
        }
        addHandler("frames-per-word") {
            PsmFramesPerWord(it.toInt())
        }
        addHandler("right-margin") {
            PsmRightMargin(it.toInt())
        }
        addHandler("left-margin") {
            PsmLeftMargin(it.toInt())
        }
        addHandler("instant") {
            PsmInstant(it.toBooleanHandleBlank(true))
        }
        addHandler("chomp") {
            PsmChomp(it.toBooleanHandleBlank(true))
        }
        addHandler("font") {
            PsmFont(it)
        }

        addHandler("clrf", "nl", "newline") {
            handleNewline(it)
            null
        }


        addHandler("@", "colour") {
            val colourName = it.lowercase()

            PsmColour(if (colourName in NAMED_COLOURS) {
                NAMED_COLOURS[colourName]!!
            } else {
                val c = Color().also { it.a = 1.0f }
                Color.rgb888ToColor(c, it.toInt(radix = 16))
                c
            })
        }

        addHandler("¬", "effects") {
            val effectNames = it.split("+")
            val effects = effectNames.mapTo(mutableSetOf()) { name ->
                TextualNode.Effect.entries.find { effect -> effect.name.lowercase() == name.lowercase() }
                    ?: throw PsmSyntaxError("No such effect: $name")
            }
            PsmEffect(effects)
        }

        addHandler("link-colour") {
            PsmColourButtonLink(it.toBooleanHandleBlank(true))
        }
        addHandler("`", "button") {
            PsmButton(it)
        }
    }

    // State variables:
    /// The frame that the next created node will start from.
    private var frameCounter = 0
    /// The current length of the processed line.
    private var lineLength = 0

    private fun handleNewline(value: String) {
        var count = if (value.isBlank()) 1 else value.toInt()
        val node = currentNodes.lastOrNull()

        // set the newline flag of the previous node if it didn't otherwise cause a newline.
        if (node != null && !node.causesNewline) {
            node.causesNewline = true
            this.lineLength = 0
            count--
        }

        // add extra newline nodes instead
        var lateFrameCounter = frameCounter
        for (i in 0 until count) {
            // add blank nodes with no frame count.
            val nlNode = TextualNode(
                text = " ", startFrame = frameCounter, endFrame = frameCounter,
                causesNewline = true
            )

            // only add newline linger frames to the node following a node that:
            // 1) didn't have any linger frames of its own
            // 2) caused a newline
            // 3) was a textual node (i.e. a non-blank node).
            // this means that double-newlines get linger frames, single newlines don't.

            val lastNode = currentNodes.lastOrNull()
            if (
                !state.instant &&
                state.newlineLinger &&
                lastNode != null &&
                lastNode.causesNewline &&
                lastNode.text.isNotBlank()
            ) {
                val lastFrameCount = lastNode.endFrame - lastNode.startFrame
                if (lastFrameCount <= state.framesPerWord) {
                    lateFrameCounter += state.newlineLingerFrames
                }
            }

            currentNodes.add(nlNode)
        }

        if (!state.instant) {
            frameCounter = lateFrameCounter
        }
    }

    private fun handleDirective(temporary: Boolean) {
        val parsed = mutableMapOf<String, String>()
        // double dollar (or #$), pop node
        when (tokenizer.consume()) {
            PsmToken.Dollar -> {
                state.pop()
                return
            }
            PsmToken.LeftBracket -> Unit  // intentionally empty
            else -> {
                throw PsmSyntaxError(
                    "expected [ after raw '$' or '#' token, use a backslash to escape it"
                )
            }
        }

        while (true) {
            val next = tokenizer.consumeIgnoringSpaces()

            if (next !is PsmToken.Text) {
                throw PsmSyntaxError("Expected a textual node when parsing a directive")
            }

            val name = next.value.lowercase()
            if (tokenizer.consumeIgnoringSpaces() !is PsmToken.Equals) {
                throw PsmSyntaxError("Expected an equals sign when parsing a directive value")
            }

            val valueToken = tokenizer.consumeIgnoringSpaces()
            when (valueToken) {
                is PsmToken.Text -> {
                    // consume any full-stops e.g. for scene names
                    val text = StringBuilder(valueToken.value)
                    while (true) {
                        if (tokenizer.peek() == PsmToken.FullStop) {
                            text.append('.')
                            tokenizer.consume()
                        } else if (tokenizer.peek() is PsmToken.Text) {
                            text.append(tokenizer.consume().value)
                        } else {
                            break
                        }
                    }

                    parsed[name] = text.toString()
                }
                is PsmToken.Comma -> {
                    throw PsmSyntaxError("Directive '${name}' is missing a value!")
                }
                // allow conveniences like $$
                is PsmToken.RightBracket -> {
                    parsed[name] = ""
                    break
                }
                else -> {
                    throw PsmSyntaxError("Unexpected token $valueToken when parsing a directive value!")
                }
            }


            parsed[name] = valueToken.value

            val final = tokenizer.consumeIgnoringSpaces()
            if (final == PsmToken.RightBracket) break
            else if (final != PsmToken.Comma) {
                throw PsmSyntaxError("Expected a comma when parsing a directive value")
            }
        }

        // strip a single extra newline after directives to make raw PSM more readable.
        if (tokenizer.peek() == PsmToken.Newline) {
            tokenizer.consume()
        }

        val entries = mutableListOf<PsmStateEntry<*>>()
        val entry = PsmState.PsmStateNode(entries)
        if (temporary) {
            state.temp = entry
        } else {
            state.push(entry)
        }

        for ((key, value) in parsed) {
            val handler = directiveHandlers[key] ?: throw PsmSyntaxError("Unknown directive $key")
            val maybeEntry = handler.invoke(value)
            if (maybeEntry != null) entries.add(maybeEntry)
        }

        if (entries.isEmpty()) {
            if (temporary) state.temp = null
            else state.pop()
        }
    }

    private fun handleTextualNode(token: PsmToken.Text) {
        val text = StringBuilder(token.value)
        var linger = state.lingerFrames
        while (!tokenizer.peek().isWhitespace() && tokenizer.peek() != PsmToken.EndOfScene) {
            val nextToken = tokenizer.consume()
            text.append(nextToken.value)

            if (linger <= 0) {
                if (nextToken == PsmToken.Comma) {
                    linger = 10
                } else if (nextToken == PsmToken.FullStop) {
                    linger = 20
                }
            }
        }

        var effects = state.effect
        if (effects.isEmpty()) {
            effects = emptySet()
        }

        val node = TextualNode(
            text.toString(),
            startFrame = frameCounter,
            endFrame = frameCounter + state.framesPerWord,
            colour = state.colour,
            causesSpace = !state.chomp,
            effects = effects,
            fontName = state.font,
        )

        // update button status
        if (state.button != null) {
            node.buttonId = state.button

            if (state.colourButtonLink) {
                node.colourLinkedToButton = true
                node.colour = null
            }
        }

        // only update frame counter if we're not in instant mode.
        // otherwise, the node gets no frames and the frame counter is static.
        if (state.instant) {
            node.endFrame = frameCounter
        } else {
            frameCounter = node.endFrame + linger
        }

        // check for word wrapping
        if (lineLength + node.text.length >= state.rightMargin) {
            val last = currentNodes.lastOrNull()
            last?.let { it.causesNewline = true; it.causesSpace = false }
            lineLength = 0
        }

        // add left-margin after newlines
        if (currentNodes.lastOrNull()?.causesNewline != false) {
            node.padding = state.leftMargin
            lineLength += state.leftMargin
        }
        lineLength += node.text.length

        currentNodes.add(node)

    }

    private fun handle(token: PsmToken){
        when (token) {
            is PsmToken.Newline -> {
                // newlines are ignored
            }
            is PsmToken.Text -> {
                handleTextualNode(token)
                state.removeTemp()
            }
            is PsmToken.Dollar -> {
                handleDirective(temporary = false)
            }
            is PsmToken.Sharp -> {
                handleDirective(temporary = true)
            }
            is PsmToken.Space -> {
                // strip leading spaces
                if (this.lineLength > 0) {
                    this.lineLength += 1
                }
            }
            else -> {
                throw PsmSyntaxError("unexpected token $token")
            }
        }
    }

    /**
     * Bakes a single scene into a list of textual nodes.
     */
    public fun bake(sceneText: String): List<TextualNode> {
        // reset state
        this.currentNodes.clear()
        this.frameCounter = 0
        this.state.clear()
        this.lineLength = 0

        tokenizer.tokenise(sceneText)

        while (true) {
            val token = tokenizer.consume()
            if (token == PsmToken.EndOfScene) break
            try {
                handle(token)
            } catch (e: Exception) {
                throw SS76EngineInternalError("Failed to handle token '$token'!", e)
            }
        }

        return this.currentNodes.toList()
    }
}