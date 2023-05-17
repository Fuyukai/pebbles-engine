/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import okio.BufferedSink
import okio.BufferedSource

// TODO: we want to have a proper interface with sections.
// probably use the following format:
//
// == Header ==
// magic number: ss76
// engine version: short (2b)
// game namespace: pascal string
// game version: short (2b)
// section table count: int (4b)
// section entry name: pascal string
// section entry size: int
//
// then section entries are stored inline and are system-specific.

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
}