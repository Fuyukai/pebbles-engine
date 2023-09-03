/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import okio.BufferedSink
import okio.BufferedSource
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.saving.Saveable

/**
 * Manages event flags. These are simple flags that can be set to a number.
 */
public class EventFlags(private val state: EngineState) : Saveable {
    private val flags = mutableMapOf<String, Int>()

    /**
     * Sets an event flag.
     */
    public fun set(flag: String, value: Int = 1) {
        if (value == 0) flags.remove(flag)

        flags[flag] = value
    }

    /**
     * Increments the value of an event flag.
     */
    public fun increment(flag: String) {
        if (isSet(flag)) {
            set(flag, getValue(flag) + 1)
        } else {
            set(flag)
        }
    }

    /**
     * Decrements the value of an event flag.
     */
    public fun decrement(flag: String) {
        if (isSet(flag)) {
            set(flag, getValue(flag) - 1)
        } else {
            set(flag, -1)
        }
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
    public fun isSet(flag: String): Boolean = flag in flags

    /**
     * Gets the value of an event flag. Defaults to 0 if it is unset.
     */
    public fun getValue(flag: String): Int {
        return flags.getOrDefault(flag, 0)
    }

    override fun read(buffer: BufferedSource) {
        val count = buffer.readInt()
        for (i in 0 until count) {
            val flag = buffer.readPascalString()
            val value = buffer.readInt()
            flags[flag] = value
        }
    }

    override fun write(buffer: BufferedSink) {
        buffer.writeInt(flags.size)
        for ((flag, value) in flags) {
            buffer.writePascalString(flag)
            buffer.writeInt(value)
        }
    }
}
