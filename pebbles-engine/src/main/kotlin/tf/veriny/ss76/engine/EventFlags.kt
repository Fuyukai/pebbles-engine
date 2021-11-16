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
import tf.veriny.ss76.EngineState

/**
 * Manages event flags. These are simple flags that can be set to true or false.
 */
public class EventFlags(private val state: EngineState) : Saveable {
    private val flags = mutableSetOf<String>()

    /**
     * Sets an event flag.
     */
    public fun set(flag: String) {
        flags.add(flag)
    }

    /**
     * Resets an event flag.
     */
    public fun reset(flag: String) {
        flags.remove(flag)
    }

    /**
     * Gets if an event flag is set.
     */
    public fun get(flag: String): Boolean = flag in flags

    override fun read(buffer: BufferedSource) {
        val count = buffer.readInt()
        for (i in 0 until count) {
            val flag = buffer.readPascalString()
            flags.add(flag)
        }
    }

    override fun write(buffer: BufferedSink) {
        buffer.writeInt(flags.size)
        for (flag in flags) {
            buffer.writePascalString(flag)
        }
    }
}