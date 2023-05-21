/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

import java.util.regex.Pattern
import kotlin.reflect.KFunction

// XXX: this just does NOT work for some insane reason!
private val MACRO_REGEX =
    "%(?<name>\\w+)\\((?<args>\\s*\\w+\\s*(?:,\\s*\\w+\\s*)*)\\s*\\)%".toRegex()

private val MACRO_PATTERN = Pattern.compile(
    "%(?<name>\\w+)\\((?<args>\\s*\\w+\\s*(?:,\\s*\\w+\\s*)*)\\s*\\)%",
    Pattern.MULTILINE
)

// code is not very good here. but it works.

/**
 * Responsible for tokenizing a stream of PSM into the individual tokens.
 */
public class PsmTokenizer {
    private companion object;

    private val macros = mutableMapOf<String, KFunction<String>>()
    private val tokens = ArrayDeque<PsmToken>()
    private var lastText = CharArray(0)

    private var textCursor: Int = 0

    /**
     * Adds a new simple macro expansion function.
     */
    public fun addMacro(vararg key: String, fn: KFunction<String>) {
        check(fn.parameters.all { it.type.classifier == String::class }) {
            "Macro functions must take all string arguments"
        }

        for (k in key) macros[k] = fn
    }

    // built-in macros
    internal fun macroDialogue(name: String): String {
        return "$name -- $[nl=1, left-margin=6]"
    }

    init {
        addMacro("dialogue", "dl", fn = ::macroDialogue)
    }

    /**
     * Expands macros in the format ``%macro(arg1,arg2)%`` into other strings.
     */
    private fun expandMacros(text: String): String {
        val built = StringBuilder()
        var lastIndex = 0

        val matcher = MACRO_PATTERN.matcher(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            built.append(text.substring(lastIndex, start))
            lastIndex = end

            val groups = matcher.namedGroups()
            val name = matcher.group(groups["name"]!!)
            val args = matcher.group(groups["args"]!!).split(",").map { it.trim() }
            val fn = macros[name] ?: error("Unknown macro '$name'")
            val result = fn.call(*args.toTypedArray())
            built.append(result)
        }

        built.append(text.substring(lastIndex))
        return built.toString()
    }

    private fun isCharEof(): Boolean = textCursor >= lastText.size

    private fun peekChar(): Char {
        return lastText[textCursor]
    }

    private fun consumeChar(): Char {
        return lastText[textCursor++]
    }

    private fun selectToken(char: Char): PsmToken {
        return when (char) {
            ',' -> PsmToken.Comma
            '=' -> PsmToken.Equals
            ']' -> PsmToken.RightBracket
            else -> error("illegal character $char")
        }
    }

    private fun consumeWord() {
        val word = StringBuilder()
        var next = peekChar()

        // todo: this feels like a gross misuse of peek/consume?
        while (true) {
            if (Character.isSpaceChar(next) || next == '\n') {
                val token = PsmToken.Text(word.toString())
                tokens.add(token)

                break
            } else if (next != ',' && next != '=' && next != ']') {
                word.append(next)
                consumeChar()

                if (isCharEof()) {
                    val token = PsmToken.Text(word.toString())
                    tokens.add(token)
                    tokens.add(PsmToken.EndOfScene)
                    break
                } else {
                    next = peekChar()
                }
            } else {
                val token = PsmToken.Text(word.toString())
                tokens.add(token)
                tokens.add(selectToken(next))
                consumeChar()
                break
            }
        }
    }

    /**
     * Tokenizes the given PSM into a list of tokens.
     */
    public fun tokenise(text: String) {
        val text = text.trim()
        val expanded = expandMacros(text)
        this.lastText = expanded.toCharArray()
        this.textCursor = 0
        this.tokens.clear()

        while (!isCharEof()) {
            when (peekChar()) {
                '$' -> {
                    tokens.add(PsmToken.Dollar)
                    consumeChar()
                }

                '#' -> {
                    tokens.add(PsmToken.Sharp)
                    consumeChar()
                }

                '[' -> {
                    tokens.add(PsmToken.LeftBracket)
                    consumeChar()
                }

                ']' -> {
                    tokens.add(PsmToken.RightBracket)
                    consumeChar()
                }

                ',' -> {
                    tokens.add(PsmToken.Comma)
                    consumeChar()
                }

                '=' -> {
                    tokens.add(PsmToken.Equals)
                    consumeChar()
                }

                '\n' -> {
                    tokens.add(PsmToken.Newline)
                    consumeChar()
                }
                // extra spaces are collapsed, leading/trailing ones are stripped
                // so spaces are only used inbetween words.
                ' ' -> {
                    if (tokens.lastOrNull() != PsmToken.Space) {
                        tokens.addLast(PsmToken.Space)
                    }
                    consumeChar()
                }
                '\\' -> {
                    // backslashes prevent any extra parsing, move straight onto eating the word
                    consumeChar()

                    if (!Character.isSpaceChar(peekChar())) {
                        consumeWord()
                    }
                }
                else -> consumeWord()
            }
        }

        if (this.tokens.lastOrNull() != PsmToken.EndOfScene) {
            tokens.add(PsmToken.EndOfScene)
        }
    }

    /**
     * Peeks the next token.
     */
    public fun peek(): PsmToken {
        return tokens.first()
    }

    /**
     * Consumes the next token.
     */
    public fun consume(): PsmToken {
        return tokens.removeFirst()
    }

    public fun consumeIgnoringSpaces(): PsmToken {
        while (true) {
            val token = consume()
            if (token.isWhitespace()) continue
            return token
        }
    }

    public fun isEof(): Boolean {
        return tokens.isEmpty()
    }
}

internal fun PsmTokenizer.print() {
    while (!isEof()) {
        val next = consume()
        if (next is PsmToken.Newline) {
            println(next)
        } else {
            print("$next ")
        }
    }
    repeat(2) { println() }
}

public fun main() {
    val tokeniser = PsmTokenizer()
    tokeniser.tokenise("$[this=is, a=directive] plus some extra text!")
    tokeniser.print()
}