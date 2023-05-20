/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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