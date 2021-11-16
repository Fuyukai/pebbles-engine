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

import tf.veriny.ss76.engine.FontManager

public const val DEFAULT_FRAMES_PER_WORD: Int = 5
public const val DEFAULT_NEWLINE_LINGER: Int = 30
private val DIRECTIVE_RE = ":(.+):(.*)".toRegex()

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
public fun splitScene(text: String, rightMargin: Int = 70, v: Boolean = false): List<TextualNode> {
    val nodes = mutableListOf<TextualNode>()

    // State variables
    //  The frame that the current node will be rendered on.
    var frameCounter = 0
    //  The current number of frames per word.
    var currentFramesPerWord = DEFAULT_FRAMES_PER_WORD
    //  The length of the current line.
    var currentLineLength: Int
    //  The number of frames to linger until the next token. Reset to zero each loop.
    var lingerFrames = 0
    //  The name of the current font.
    var currentFont = "default"

    // pushed data, automatically used during tokenization
    val pushed = ArrayDeque<Token>()


    val lines = text.split("\n")
    for (line in lines) {
        currentLineLength = 0

        val words = line.split(" ").toMutableList()
        // don't process empty lines. this causes an extra empty token to appear.
        if (words.size == 1 && words[0].isBlank()) words.clear()

        for (rawWord in words) {
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
                        if (token.text.isNotEmpty()) throw TokenizationException("cannot push markup '$dValue' with text")
                        pushed.addLast(token)
                    }
                    // pops colour/effect/links
                    "pop" -> {
                        if (dValue.isNotEmpty()) {
                            throw TokenizationException("pop token accidentally consumes $dValue")
                        }
                        pushed.removeLast()
                    }
                    // adds n frames to timing
                    "linger" -> {
                        lingerFrames = if (dValue.isEmpty()) 60
                        else dValue.toInt()
                    }
                    // changes frames per word calculation
                    "fpw" -> {
                        currentFramesPerWord = if (dValue == "reset" || dValue == "0") DEFAULT_FRAMES_PER_WORD
                        else dValue.toInt()
                    }
                    // changes active font
                    "font" -> {
                        if (dValue.isEmpty()) throw TokenizationException("font directive must have font name")
                        currentFont = dValue
                    }
                    // removes previous space
                    "chomp" -> {
                        val lastNode =
                            nodes.lastOrNull() ?: throw TokenizationException("can't chomp first node")
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

            val start: Int
            val end: Int
            if (token.text.isBlank()) {
                start = frameCounter
                end = frameCounter
            } else {
                start = frameCounter + lingerFrames
                end = (start + currentFramesPerWord).also { frameCounter = it }
            }

            // calculate effects first; we need to know if a dialogue effect is active to insert
            // causesNewline and update padding early
            val parsedEffects =
                token.effects.mapTo(mutableSetOf()) { TextualNode.Effect.valueOf(it.uppercase()) }
            val isDialogue = TextualNode.Effect.DIALOGUE in parsedEffects
            // useless effect for the renderer. kept anyway when outputting the unravelled nodes
            // parsedEffects.remove(TextualNode.Effect.DIALOGUE)

            val word = token.text
            val nextLength = currentLineLength + word.length + 1

            // update previous node, if needed
            val overflowed =
                //(isDialogue && nextLength >= (rightMargin - 7)) ||
                (nextLength >= rightMargin)

            if (overflowed) {
                val last = nodes.lastOrNull()
                last?.causesNewline = true
                last?.causesSpace = false
                currentLineLength = word.length + 1
            } else {
                currentLineLength = nextLength
            }

            // now we can finally create the node and update its properties
            val node = TextualNode(
                token.text, startFrame = start, endFrame = end,
                causesNewline = token.hasNewline,
                causesSpace = !token.hasNewline,
                buttonId = token.buttonName,
                effects = parsedEffects,
                font = currentFont,
            )

            // first dialogue line on an overflowed line always has 7 characters of padding
            if (isDialogue && overflowed) {
                currentLineLength += 6
                node.padding = 6
            }

            // calc colour if needed
            val colourName = token.colour?.lowercase() ?: "white"
            val colour = (if (colourName == "linked") {
                if (token.buttonName == null) {
                    error("cannot have 'linked' colour token and a null button name")
                } else {
                    node.colourLinkedToButton = true
                    FontManager.COLOURS["white"]
                }
            } else FontManager.COLOURS[token.colour ?: "white"])

            if (colour == null) {
                val validColours = FontManager.COLOURS.keys.joinToString(", ")
                throw TokenizationException("invalid colour: $colour\nvalid colours: $validColours")
            }

            node.colour = colour
            nodes.add(node)
            lingerFrames = 0
        }

        // todo: allow disabling newline linger
        val newlineNode = TextualNode(
            "", startFrame = frameCounter, endFrame = frameCounter, causesNewline = true,
            causesSpace = false,
        )

        val lastNode = nodes.lastOrNull()
        // avoid adding extra frames to extra newlines
        if (lastNode?.causesNewline == false && lingerFrames <= 0) {
            frameCounter += DEFAULT_NEWLINE_LINGER
        }
        nodes.add(newlineNode)
    }

    return nodes
}

public fun main(args: Array<String>) {
    val sceneText = ":push:@green@ this text is green :push:¬shake¬ and this shakes too :pop: no more shaking :pop:"

    println(splitScene(sceneText).joinToString(" ") { it.repr() })
}