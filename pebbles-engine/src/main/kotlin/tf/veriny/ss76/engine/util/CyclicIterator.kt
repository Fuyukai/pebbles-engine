/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

public class CyclicIterator<T>(private val original: Iterable<T>) : Iterator<T> {
    private var instance = original.iterator()

    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): T {
        if (!instance.hasNext()) instance = original.iterator()
        return instance.next()
    }
}

/**
 * Gets an infinitely cyclable iterator.
 */
public fun <T> Iterable<T>.cycle(): Iterator<T> {
    return CyclicIterator(this)
}