/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

/**
 * Thrown for internal errors in the engine.
 */
public class SS76EngineInternalError(
    message: String, cause: Throwable? = null
) : RuntimeException(message, cause)