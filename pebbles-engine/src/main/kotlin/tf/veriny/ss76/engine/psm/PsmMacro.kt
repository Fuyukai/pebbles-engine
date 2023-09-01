/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

/**
 * A macro in a PSM file that expands out to other text.
 */
public fun interface PsmMacro {
    /**
     * Invokes this macro, returning the expanded text.
     */
    public operator fun invoke(vararg args: String): String
}