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

package tf.veriny.ss76.engine.util

@OptIn(ExperimentalStdlibApi::class)
private fun murmurhash3_scramble(value: UInt): UInt {
    var value = value
    value *= 0xcc9e2d51u
    value.rotateLeft(15)
    value *= 0x1b873593u
    return value
}

@OptIn(ExperimentalStdlibApi::class)
public fun murmurhash3_32(x: Int, y: Int): Int {
    var x = x.toUInt()
    var y = y.toUInt()
    // seed randomly chosen by me
    var hash = 8605873u

    x = murmurhash3_scramble(x)
    hash = hash.xor(x)
    hash = hash.rotateLeft(13)
    hash = (hash * 5u) + 0xe6546b64u

    y = murmurhash3_scramble(y)
    hash = hash.xor(y)
    hash = hash.rotateLeft(13)
    hash = (hash * 5u) + 0xe6546b64u

    hash = hash.xor(/*len*/8u)
    hash = hash.xor(hash.shr(16))
    hash *= 0x85ebca6bu
    hash = hash.xor(hash.shr(13))
    hash *= 0xc2b2ae35u
    hash = hash.xor(hash.shr(16))

    return hash.toInt()
}