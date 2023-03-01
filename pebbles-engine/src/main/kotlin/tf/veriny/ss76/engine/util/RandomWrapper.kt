/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

import squidpony.squidmath.RandomnessSource
import kotlin.random.Random

public class RandomWrapper(public val source: RandomnessSource) : Random() {
    override fun nextBits(bitCount: Int): Int {
        return source.next(bitCount)
    }

    override fun nextLong(): Long {
        return source.nextLong()
    }
}
