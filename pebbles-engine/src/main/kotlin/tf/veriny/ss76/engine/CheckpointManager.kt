/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import dev.dirs.BaseDirectories
import okio.buffer
import okio.sink
import okio.source
import tf.veriny.ss76.EngineState
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.getLastModifiedTime

/**
 * Responsible for saving checkpoints.
 */
public class CheckpointManager(
    private val state: EngineState,
    private val namespace: String,
) {
    public companion object {
        public val BASE_DIR: Path

        init {
            val bb = BaseDirectories.get().dataDir
            BASE_DIR = if (bb == null) {
                // windows shit...
                val dir = Path.of(System.getenv("APPDATA"))
                dir.resolve("pebbles")
            } else {
                Path.of(bb).resolve("pebbles")
            }
        }

        private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss")

        public const val CHECKPOINT_VERSION: Int = 2
    }

    /** The directory to use for saving. */
    private val saveDir = BASE_DIR.resolve(namespace).also { it.createDirectories() }

    /** The menu for checkpoints. */
    private val checkpointScene = UpdatableSceneWrapper("save-menu")

    private inner class CheckpointButton(val idx: Int, val load: Boolean) : Button {
        override val name: String = if (load) "load-$idx" else "save-$idx"

        override fun run(state: EngineState) {
            if (load) loadCheckpoint(idx)
            else saveCheckpoint(idx)
        }
    }

    public fun register() {
        updateCheckpointScene()
    }

    private fun getCheckpointDate(number: Int): FileTime? {
        val file = saveDir.resolve("checkpoint.$number.dat")
        return try {
            file.getLastModifiedTime()
        } catch (e: IOException) {
            null
        }
    }

    private fun updateCheckpointScene() {
        checkpointScene.reset()
        checkpointScene.edit(0) {
            line("@slate@Checkpoint @slate@Menu")
            newline()

            line(
                "A checkpoint saves the current state of your reading on a particular route. " +
                "When loading a checkpoint, the current scene and scene stack are restored. " +
                "Thus, you can save anywhere, no matter how deep."
            )
            newline()

            for (idx in 0 until 5) {
                val time = getCheckpointDate(idx)
                if (time == null) {
                    line("Slot #$idx - @green@empty - @salmon@`save-$idx`SAVE")
                } else {
                    val zone = ZoneId.systemDefault()
                    val it = time.toInstant().atZone(zone)
                    val ts = it.format(formatter)

                    line("Slot #$idx - :push:@sky@ $ts :pop: - @salmon@`load-$idx`LOAD / @salmon@`save-$idx`SAVE")
                }

                checkpointScene.addButton(CheckpointButton(idx, true))
                checkpointScene.addButton(CheckpointButton(idx, false))
            }

            newline()
            backButton()
        }

        checkpointScene.register(state.sceneManager)
    }

    /**
     * Writes the checkpoint with the specified ID.
     */
    public fun saveCheckpoint(idx: Int) {
        val file = saveDir.resolve("checkpoint.$idx.dat")
        Files.newOutputStream(file, CREATE, TRUNCATE_EXISTING).use {
            val sink = it.sink().buffer()

            // magic number
            sink.writeUtf8("SS76")
            // version
            sink.writeInt(CHECKPOINT_VERSION)

            // write scene stack
            state.sceneManager.write(sink)
            state.eventFlagsManager.write(sink)

            sink.flush()
            sink.close()
        }

        updateCheckpointScene()
    }

    /**
     * Saves the checkpoint with the specified ID.
     */
    public fun loadCheckpoint(idx: Int) {
        val file = saveDir.resolve("checkpoint.$idx.dat")
        Files.newInputStream(file).use {
            val source = it.source().buffer()

            check(source.readUtf8(4L) == "SS76") { "checkpoint file missing magic" }
            val version = source.readInt()
            check(version == CHECKPOINT_VERSION) { "invalid checkpoint version $version" }

            state.sceneManager.read(source)
            state.eventFlagsManager.read(source)
        }

        // pop the checkpoint scene off
        if (state.sceneManager.currentSceneIs("save-menu")) {
            state.sceneManager.exitScene()
        }
    }
}