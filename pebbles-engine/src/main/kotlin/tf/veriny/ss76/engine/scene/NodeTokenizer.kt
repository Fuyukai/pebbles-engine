/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.engine.scene.TextualNode.Effect
import tf.veriny.ss76.engine.util.NAMED_COLOURS

public const val DEFAULT_FRAMES_PER_WORD: Int = 5
public const val DEFAULT_NEWLINE_LINGER: Int = 45
private val DIRECTIVE_RE = ":([\\w-]+):(\\S*)".toRegex()

/**
 * A single token ready to be transformed into a TextualNode.
 */
public data class Token(
    /** The colour name. */
    public val colour: String?,

    /** The set of effects. */
    public val effects: Set<String>,

    /** The button associated with the token. */
    public val buttonName: String?,

    /** The text of the token. */
    public val text: String,

    /** If this token has a newline. */
    public val hasNewline: Boolean = false,
)

private enum class TokenifyState {
    BEGIN,
    TEXT,
    COLOUR,
    EFFECT,
    BUTTON,
    ESCAPE,
}

/**
 * Base class thrown for tokenisation errors.
 */
public open class TokenizationException(
    message: String? = null, cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a token has a bad modifier.
 */
public class BadModifierStateException(
    message: String? = null, cause: Throwable? = null
) : TokenizationException(message, cause)

/**
 * Thrown when a directive is unknown.
 */
public class UnknownDirectiveException(
    directive: String, cause: Throwable? = null,
) : TokenizationException("Unknown directive: $directive", cause)

private fun String.toIntToken(): Int = try {
    toInt()
} catch (e: NumberFormatException) {
    throw TokenizationException("Can't turn '$this' into a number", e)
}

/**
 * Tokenifies the word.
 */
private fun tokenify(
    currentColour: String? = null,
    currentEffects: Set<String> = setOf(),
    currentButton: String? = null,
    word: String
): Token {
    // This is a terrible function.
    var colour = currentColour
    val effects = currentEffects.toMutableSet()
    var button = currentButton
    val wordBuilder = StringBuilder()

    val currentBuilder = StringBuilder()
    var state = TokenifyState.BEGIN

    var hasNewline = false

    for (char/*lotte*/ in word) {
        if (hasNewline) {
            throw BadModifierStateException("Got character '$char' after newline")
        }

        if (state == TokenifyState.TEXT) {
            wordBuilder.append(char)
            continue
        }

        when (char) {
            '\\' -> {
                // forces this token into being literal
                state = TokenifyState.TEXT
            }

            '@' -> {
                state = when (state) {
                    TokenifyState.BEGIN -> TokenifyState.COLOUR
                    TokenifyState.COLOUR -> {
                        colour = currentBuilder.toString()
                        currentBuilder.clear()
                        TokenifyState.BEGIN
                    }

                    else -> throw BadModifierStateException("Can't handle '@' during state $state")
                }
            }

            '¬' -> {
                state = when (state) {
                    TokenifyState.BEGIN -> TokenifyState.EFFECT
                    TokenifyState.EFFECT -> {
                        if (currentBuilder.isNotEmpty()) {
                            effects.add(currentBuilder.toString())
                        }
                        currentBuilder.clear()
                        TokenifyState.BEGIN
                    }

                    else -> throw BadModifierStateException("Can't handle '¬' during state $state")
                }
            }

            ',' -> {
                when (state) {
                    TokenifyState.BEGIN, TokenifyState.TEXT -> {
                        wordBuilder.append(char)
                        state = TokenifyState.TEXT
                    }

                    TokenifyState.EFFECT -> {
                        effects.add(currentBuilder.toString())
                        currentBuilder.clear()
                    }

                    else -> throw BadModifierStateException("Can't handle ',' during state $state")
                }
            }

            '`' -> {
                state = when (state) {
                    TokenifyState.BEGIN -> TokenifyState.BUTTON
                    TokenifyState.BUTTON -> {
                        button = currentBuilder.toString()
                        currentBuilder.clear()
                        TokenifyState.BEGIN
                    }

                    else -> throw BadModifierStateException("Can't handle '`' during state $state")
                }
            }

            '\n' -> {
                if (state != TokenifyState.TEXT && state != TokenifyState.BEGIN) {
                    throw BadModifierStateException("Can't handle newline during state $state")
                }
                hasNewline = true
            }

            else -> {
                when (state) {
                    TokenifyState.BEGIN, TokenifyState.TEXT -> {
                        wordBuilder.append(char)
                    }

                    TokenifyState.EFFECT, TokenifyState.BUTTON, TokenifyState.COLOUR -> {
                        currentBuilder.append(char)
                    }

                    else -> TODO("what?")
                }
            }
        }
    }

    return when (state) {
        TokenifyState.COLOUR -> {
            throw BadModifierStateException("Missing closing '@' in $word")
        }

        TokenifyState.EFFECT -> {
            throw BadModifierStateException("Missing closing '¬' in $word")
        }

        TokenifyState.BUTTON -> {
            throw BadModifierStateException("Missing closing '`' in $word")
        }

        else -> {
            val word = wordBuilder.toString()
            Token(colour, effects, button, word, hasNewline = hasNewline)
        }
    }
}

/**
 * Splits a single scene into a stream of TextualNode directives.
 */
@Throws(TokenizationException::class)
public fun tokenifyScene(
    text: String,
    rightMargin: Int = 70,
    defaultFramesPerWord: Int = DEFAULT_FRAMES_PER_WORD,
    v: Boolean = false
): List<TextualNode> {
    val nodes = mutableListOf<TextualNode>()

    // State variables
    var startingFrame = 0
    //  The frame that the current node will be rendered on.
    var frameCounter = 0
    //  The current number of frames per word.
    var currentFramesPerWord = defaultFramesPerWord
    //  The length of the current line.
    var currentLineLength: Int
    //  The number of frames to linger until the next token. Reset to zero each loop.
    var lingerFrames = 0
    //  The name of the current font.
    var currentFont = "default"
    //  If we're currently using newline linger.
    var isUsingNewlineLinger = true
    //  The frame counters that marks have been stored for.
    val markedFrameCounters = IntArray(16)
    //  The last font that we used.
    var lastFont = "default"


    // pushed data, automatically used during tokenization
    val pushed = ArrayDeque<Token>()


    val lines = text.split("\n")
    for (line in lines) {
        currentLineLength = 0

        val words = line.split(" ").toMutableList()
        // don't process empty lines. this causes an extra empty token to appear.
        if (words.size == 1 && words[0].isBlank()) words.clear()

        val wordIterator = words.iterator()

        for (rawWord in wordIterator) {
            // directives include stuff like :push: or :pop:.
            val directive = DIRECTIVE_RE.matchEntire(rawWord)
            if (directive != null) {
                val dName = directive.groups[1]!!.value
                val dValue = directive.groups[2]!!.value

                when (dName) {
                    // pushes colour/effect/link
                    "push" -> {
                        val tos = pushed.lastOrNull()
                        val token = tokenify(
                            tos?.colour, tos?.effects ?: setOf(),
                            tos?.buttonName, dValue
                        )
                        if (token.text.isNotEmpty()) {
                            throw TokenizationException("cannot push markup '$dValue' with text")
                        }
                        pushed.addLast(token)
                    }

                    // pops colour/effect/links
                    "pop" -> {
                        if (dValue.isNotEmpty()) {
                            throw TokenizationException("pop token accidentally consumes $dValue")
                        }
                        if (pushed.isEmpty()) {
                            throw TokenizationException("reached pop token with nothing to pop")
                        }
                        pushed.removeLast()
                    }

                    // enables or disables automatic newline lingering
                    "newline-linger" -> {
                        val should = try {
                            dValue.toBooleanStrict()
                        } catch (e: IllegalArgumentException) {
                            throw TokenizationException(
                                "newline-linger takes true or false, not '$dValue'",
                                e
                            )
                        }

                        isUsingNewlineLinger = should
                    }

                    // adds n frames to timing
                    "linger" -> {
                        lingerFrames = if (dValue.isEmpty()) 60
                        else dValue.toIntToken()
                    }

                    // changes frames per word calculation
                    "fpw" -> {
                        currentFramesPerWord = if (dValue == "reset" || dValue == "0") {
                            DEFAULT_FRAMES_PER_WORD
                        } else {
                            dValue.toIntToken()
                        }
                    }

                    // changes active font
                    "font" -> {
                        if (dValue.isEmpty()) {
                            throw TokenizationException("font directive must have font name")
                        }
                        if (dValue == "reset") {
                            currentFont = lastFont
                        } else {
                            lastFont = currentFont
                            currentFont = dValue
                        }
                    }

                    // marks the current frame counter to be restored
                    "mark-fc" -> {
                        val slot = dValue.toIntToken()
                        markedFrameCounters[slot] = lingerFrames + frameCounter
                    }

                    // restores the specified frame counter
                    "restore-fc" -> {
                        val slot = dValue.toIntToken()
                        // ignore linger entirely, we inherit it from the previous one
                        lingerFrames = 0
                        frameCounter = markedFrameCounters[slot]
                    }

                    // sets the frame counter to an absolute value
                    "set-fc" -> {
                        val time = dValue.toIntToken()
                        frameCounter = time
                    }

                    // removes previous space
                    "chomp" -> {
                        val lastNode =
                            nodes.lastOrNull()
                            ?: throw TokenizationException("can't chomp first node")
                        lastNode.causesSpace = false
                    }

                    else -> throw UnknownDirectiveException(dName)
                }

                continue
            }

            val tos = pushed.lastOrNull()
            val token = tokenify(
                tos?.colour, tos?.effects ?: setOf(),
                tos?.buttonName, rawWord
            )

            // calculate effects first; we need to know if a dialogue effect is active to insert
            // causesNewline and update padding early
            val parsedEffects =
                token.effects.mapTo(mutableSetOf()) { Effect.valueOf(it.uppercase()) }

            val isInstant = Effect.INSTANT in parsedEffects
            val isDialogue = Effect.DIALOGUE in parsedEffects

            var start: Int
            var end: Int
            if (isInstant || token.text.isBlank()) {
                start = frameCounter
                end = frameCounter
            } else {
                start = frameCounter + lingerFrames
                end = (start + currentFramesPerWord).also { frameCounter = it }
            }

            // useless effect for the renderer. kept anyway when outputting the unravelled nodes
            // parsedEffects.remove(TextualNode.Effect.DIALOGUE)

            val word = token.text
            val nextLength = currentLineLength + word.length + 1

            // update previous node, if needed
            val hasOverflowed =
                //(isDialogue && nextLength >= (rightMargin - 7)) ||
                (nextLength >= rightMargin)

            val lastNode = nodes.lastOrNull()
            if (hasOverflowed) {
                lastNode?.causesNewline = true
                lastNode?.causesSpace = false
                currentLineLength = word.length + 1
            } else {
                currentLineLength = nextLength
            }


            // now we can finally create the node and update its properties
            val textualNode = TextualNode(
                token.text, startFrame = start, endFrame = end,
                causesNewline = token.hasNewline,
                causesSpace = !token.hasNewline,
                buttonId = token.buttonName,
                effects = parsedEffects,
                fontName = currentFont,
            )

            // first dialogue nodes per line always have 7 characters of padding
            if (isDialogue && lastNode?.causesNewline == true) {
                currentLineLength += 6
                textualNode.padding = 6
            }

            // update linkage and copy over colour
            if (token.colour != null) {
                if (token.colour == "linked") {
                    if (token.buttonName == null) {
                        throw TokenizationException("Cannot have 'linked' colour token but no button name")
                    }
                    textualNode.colourLinkedToButton = true
                } else {
                    textualNode.colour = NAMED_COLOURS[token.colour]
                                         ?: throw TokenizationException("No such colour: ${token.colour}")
                }
            }

            nodes.add(textualNode)
            lingerFrames = 0
        }

        val newlineNode = TextualNode(
            "", startFrame = frameCounter, endFrame = frameCounter, causesNewline = true,
            causesSpace = false,
        )

        val lastNode = nodes.lastOrNull()
        // avoid adding extra frames to extra newlines
        if (isUsingNewlineLinger && (lastNode?.causesNewline == false && lingerFrames <= 0)) {
            frameCounter += DEFAULT_NEWLINE_LINGER
        }

        nodes.add(newlineNode)
    }

    return nodes
}

public fun main(args: Array<String>) {
    val sceneText = ":push:@green@ this text is green :push:¬shake¬ and this shakes too :pop: no more shaking :pop:"

    println(tokenifyScene(sceneText).joinToString(" "))
}
