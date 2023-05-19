/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import okio.BufferedSink
import okio.BufferedSource

/**
 * Defines something that is saved to a checkpoint file.
 */
public interface Saveable {
    /**
     * Writes the data for this Saveable to the buffer.
     */
    public fun write(buffer: BufferedSink)

    /**
     * Reads the data for this Saveable from the buffer.
     */
    public fun read(buffer: BufferedSource)

    /**
     * Called immediately after loading. Use this for when you depend on other [Saveable] instances
     * loading.
     */
    public fun postLoad() {

    }
}