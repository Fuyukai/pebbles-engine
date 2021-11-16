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

package tf.veriny.ss76.engine

import okio.BufferedSink
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import kotlin.experimental.xor

private const val XOR_CONSTANT: Byte = 39

/**
 * Writes a pascal (length-prefixed) string to the stream.
 */
public fun BufferedSink.writePascalString(s: String) {
    writeInt(s.length)
    val encoded = s.encodeToByteArray()
    for (byte in encoded) {
        val b = byte.xor(XOR_CONSTANT)
        writeByte(b.toInt())
    }
}

/**
 * Reads a pascal (length-prefixed) string from the stream.
 */
public fun BufferedSource.readPascalString(): String {
    val length = readInt()
    val buf = ByteArray(length)

    for (i in 0 until length) {
        val byt = readByte().xor(XOR_CONSTANT)
        buf[i] = byt
    }

    return buf.decodeToString()
}