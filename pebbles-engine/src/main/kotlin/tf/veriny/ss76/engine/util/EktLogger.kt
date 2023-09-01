/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

import com.badlogic.gdx.ApplicationLogger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

/**
 * Adapter for the LibGDX [ApplicationLogger] that simply forwards it to Log4j2.
 */
public object EktLogger : ApplicationLogger {
    private fun baseLog(level: Level, tag: String, message: String, throwable: Throwable? = null) {
        val logger = LogManager.getLogger(tag)
        if (throwable == null) {
            logger.log(level, message)
        } else {
            logger.log(level, message, throwable)
        }
    }

    override fun log(tag: String, message: String): Unit = baseLog(Level.INFO, tag, message)
    override fun log(tag: String, message: String, exception: Throwable): Unit =
        baseLog(Level.INFO, tag, message, exception)

    override fun debug(tag: String, message: String): Unit = baseLog(Level.DEBUG, tag, message)
    override fun debug(tag: String, message: String, exception: Throwable): Unit =
        baseLog(Level.INFO, tag, message, exception)

    override fun error(tag: String, message: String): Unit = baseLog(Level.ERROR, tag, message)
    override fun error(tag: String, message: String, exception: Throwable): Unit =
        baseLog(Level.ERROR, tag, message, exception)
}