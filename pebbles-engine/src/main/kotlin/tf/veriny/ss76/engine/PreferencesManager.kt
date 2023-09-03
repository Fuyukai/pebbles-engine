/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import tf.veriny.ss76.SS76Settings
import tf.veriny.ss76.engine.saving.SaveManager
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer

/**
 * Handles saving and loading certain global preferences.
 */
public class PreferencesManager(private val settings: SS76Settings) {
    private companion object {
        private val DEFAULTS = mutableMapOf(
            "language" to "en",
            "override-text-skip-settings" to "false",
        )
    }

    private val prefsDir = SaveManager.BASE_DIR.resolve(settings.namespace).also {
        it.createDirectories()
    }
    
    private val actualProperties = mutableMapOf<String, String>()

    // internal as this is only ever called during engine startup
    internal fun loadPreferences() {
        val props = Properties()
        val file = prefsDir.resolve("engine.properties")
        if (!file.exists()) {
            DEFAULTS.forEach { (t, u) -> actualProperties[t] = u }
            return save()
        }

        file.reader(Charsets.UTF_8).use { props.load(it) }
        props.forEach { (k, v) -> actualProperties[(k as String)] = v as String }
    }

    public operator fun get(prop: String): String {
        return actualProperties[prop] ?: error("No such property '$prop'")
    }

    public operator fun set(prop: String, value: String) {
        actualProperties[prop] = value
    }

    public fun save() {
        val props = Properties()
        for (prop in actualProperties) {
            props[prop.key] = prop.value
        }
        val file = prefsDir.resolve("engine.properties")
        file.writer(
            Charsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { props.store(it, "SS76 game preferences") }
    }
}