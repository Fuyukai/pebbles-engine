package tf.veriny.ss76.engine.psm

import com.badlogic.gdx.graphics.Color
import tf.veriny.ss76.engine.psm.PsmStateStack.StackEntry
import tf.veriny.ss76.engine.scene.TextualNode
import tf.veriny.ss76.engine.util.NAMED_COLOURS
import tf.veriny.ss76.toBooleanHandleBlank

/**
 * Bakes Pebbles Scene Markup into a series of [TextualNode] instances.
 */
@OptIn(ExperimentalStdlibApi::class)
public class SceneBaker {
    private companion object {
        private val DEFAULT_STATE = StackEntry(
            enableNewlineLinger = true,
            newlineLingerFrames = 45,
            framesPerWord = 5,
            rightMargin = 70,
        )
    }

    public val tokenizer: PsmTokenizer = PsmTokenizer()
    private val state = PsmStateStack(DEFAULT_STATE)
    private val currentNodes = mutableListOf<TextualNode>()

    private val directiveHandlers = mutableMapOf<String, StackEntry.(String) -> Unit>()

    private fun addHandler(vararg keys: String, handler: StackEntry.(String) -> Unit) {
        for (key in keys) {
            directiveHandlers[key] = handler
        }
    }

    init {
        addHandler("pop") {
            val count = if (it.isBlank()) 1 else it.toInt()
            // extra + 1 to remove the one immediately put on
            repeat(count + 1) { state.pop() }
        }
        addHandler("newline-linger") {
            enableNewlineLinger = it.toBooleanHandleBlank(true)
        }
        addHandler("newline-linger-frames") {
            newlineLingerFrames = it.toInt()
        }
        addHandler("frames-per-word") {
            framesPerWord = it.toInt()
        }
        addHandler("right-margin") {
            rightMargin = it.toInt()
        }
        addHandler("left-margin") {
            leftMargin = it.toInt()
        }
        addHandler("instant") {
            instant = it.toBooleanHandleBlank(true)
        }
        addHandler("chomp") {
            chomp = it.toBooleanHandleBlank(true)
        }

        addHandler("clrf", "nl", "newline", handler = ::handleNewline)


        addHandler("@", "colour") {
            val colourName = it.lowercase()

            if (colourName in NAMED_COLOURS) {
                this.colour = NAMED_COLOURS[colourName]!!
            } else {
                this.colour = Color(it.toInt(radix = 16))
            }
        }

        addHandler("¬", "effects") {
            val effectNames = it.split("+")
            val effects = effectNames.mapTo(mutableSetOf()) { name ->
                TextualNode.Effect.entries.find { effect -> effect.name.lowercase() == name.lowercase() }
                    ?: throw PsmSyntaxError("No such effect: $name")
            }
            this.effects = effects
        }

        addHandler("link-colour") { colourLinkedToButton = it.toBooleanStrictOrNull() ?: true }
        addHandler("`", "button") { lastButton = it }
    }

    // State variables:
    /// The frame that the next created node will start from.
    private var frameCounter = 0
    /// The current length of the processed line.
    private var lineLength = 0

    private fun handleNewline(entry: StackEntry, value: String) {
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
                state.enableNewlineLinger &&
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
        if (tokenizer.consume() != PsmToken.LeftBracket) {
            throw PsmSyntaxError(
                "expected [ after raw '$' or '#' token, use a backslash to escape it"
            )
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
                // allow conveniences like $[pop=]
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

        val entry = StackEntry(temp = temporary)
        state.push(entry)

        for ((key, value) in parsed) {
            val handler = directiveHandlers[key] ?: throw PsmSyntaxError("Unknown directive $key")
            handler.invoke(entry, value)
        }

    }

    private fun handleTextualNode(token: PsmToken.Text) {
        val text = StringBuilder(token.value)
        var linger = state.linger
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

        val node = TextualNode(
            text.toString(),
            startFrame = frameCounter,
            endFrame = frameCounter + state.framesPerWord,
            colour = state.colour,
            causesSpace = !state.chomp,
            effects = state.effects
        )

        // update button status
        if (state.lastButton != null) {
            node.buttonId = state.lastButton

            if (state.colourLinkedToButton) {
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
            when (val nextToken = tokenizer.consume()) {
                is PsmToken.EndOfScene -> break
                is PsmToken.Newline -> {
                    // newlines are ignored
                }
                is PsmToken.Text -> {
                    handleTextualNode(nextToken)
                    state.removeTemp()
                }
                is PsmToken.Dollar -> {
                    handleDirective(temporary = false)
                }
                is PsmToken.Sharp -> {
                    handleDirective(temporary = true)
                }
                is PsmToken.Space -> {
                    this.lineLength += 1
                }
                else -> {
                    throw PsmSyntaxError("unexpected token $nextToken")
                }
            }
        }

        return this.currentNodes
    }
}