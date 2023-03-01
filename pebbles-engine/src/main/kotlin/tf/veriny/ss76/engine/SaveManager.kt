/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import dev.dirs.BaseDirectories
import okio.ByteString.Companion.toByteString
import okio.buffer
import okio.sink
import okio.source
import tf.veriny.ss76.EngineState
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import java.nio.file.attribute.FileTime
import kotlin.io.path.createDirectories
import kotlin.io.path.getLastModifiedTime

// TODO: custom exceptions
/**
 * Handles saving persistent state.
 */
public class SaveManager(private val state: EngineState) {
    private companion object {
        val BASE_DIR: Path
        val MAGIC = "SS76".toByteArray().toByteString()
        const val SAVE_VERSION = 1

        init {
            val bb = BaseDirectories.get().dataDir
            BASE_DIR = if (bb == null) {
                // windows shit...
                val dir = Path.of(System.getenv("APPDATA"))
                dir.resolve("magellanic-gap")
            } else {
                Path.of(bb).resolve("magellanic-gap")
            }
        }

    }

    private val saveDir = BASE_DIR.resolve("magellanic-gap").also { it.createDirectories() }

    // extra subsystems that need saving
    private val needsSaving: MutableMap<String, Saveable> = mutableMapOf()

    /**
     * Adds a new [saveable] to the items to be saved. This requires an ID, which will be used to
     * identify sections in the checkpoint file.
     */
    public fun addSaveable(id: String, saveable: Saveable) {
        needsSaving[id] = saveable
    }

    public fun getSaveDate(slot: Int): FileTime? {
        val file = saveDir.resolve("checkpoint.$slot.dat")
        return try {
            file.getLastModifiedTime()
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Loads persistent state from the specified checkpoint slot.
     */
    public fun load(slot: Int) {
        val seenSegments = mutableSetOf<String>()

        val file = saveDir.resolve("checkpoint.$slot.dat")
        Files.newInputStream(file, READ).use {
            val source = it.source().buffer()

            check(source.readUtf8(4L) == "SS76") { "checkpoint file missing magic" }
            val version = source.readInt()
            check(version == SAVE_VERSION) { "invalid checkpoint version $version" }

            val segmentCount = source.readByte().toInt()
            for (idx in 0 until segmentCount) {
                val name = source.readPascalString()
                seenSegments.add(name)
                val segment = needsSaving[name] ?: error("unknown segment: $name")
                segment.read(source)
            }
        }
    }

    /**
     * Saves persistent state to the specified checkpoint slot.
     */
    public fun save(slot: Int) {
        val file = saveDir.resolve("checkpoint.$slot.dat")
        Files.newOutputStream(file, WRITE, CREATE, TRUNCATE_EXISTING).use {
            val sink = it.sink().buffer()
            sink.write(MAGIC)
            sink.writeInt(SAVE_VERSION)

            val segments = needsSaving.size
            sink.writeByte(segments)

            for ((name, value) in needsSaving) {
                sink.writePascalString(name)
                value.write(sink)
            }

            sink.flush()
            sink.close()
        }
    }
}
