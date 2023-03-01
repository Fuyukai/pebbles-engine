/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle
import com.badlogic.gdx.files.FileHandle
import java.nio.file.Path
import com.badlogic.gdx.Files as IFiles

/**
 * Replacement for ``Gdx.files`` that creates [NioFileHandle] instances.
 */
public object EktFiles : IFiles {
    /** The location for external FileHandle instances. */
    public val EXTERNAL_FILES: Path = Path.of(System.getProperty("user.home"))

    /** The singleton file handle resolver used. */
    public val RESOLVER: EktFileHandleResolver = EktFileHandleResolver()

    /**
     * Returns a handle representing a file or directory.
     */
    override fun getFileHandle(path: String, type: FileType): FileHandle {
        val fp = when (type) {
            FileType.Classpath -> RESOLVER.resolveClasspath(path)
            FileType.Absolute -> Path.of(path)
            FileType.Internal -> RESOLVER.getPath(path)
            FileType.External -> EXTERNAL_FILES.resolve(path)
            FileType.Local -> Path.of(".").resolve(path)
        }

        return if (fp == null) {
            Lwjgl3FileHandle(path, type)
        } else {
            NioFileHandle(fp, type)
        }
    }

    /** Convenience method that returns a [FileType.Classpath] file handle.  */
    override fun classpath(path: String): FileHandle =
        getFileHandle(path, FileType.Classpath)

    /** Convenience method that returns a [FileType.Internal] file handle.  */
    override fun internal(path: String): FileHandle =
        getFileHandle(path, FileType.Internal)

    /** Convenience method that returns a [FileType.External] file handle.  */
    override fun external(path: String): FileHandle =
        getFileHandle(path, FileType.External)

    /** Convenience method that returns a [FileType.Absolute] file handle.  */
    override fun absolute(path: String): FileHandle =
        getFileHandle(path, FileType.Absolute)

    /** Convenience method that returns a [FileType.Local] file handle.  */
    override fun local(path: String): FileHandle =
        getFileHandle(path, FileType.Local)

    override fun getExternalStoragePath(): String = EXTERNAL_FILES.toAbsolutePath().toString()
    override fun isExternalStorageAvailable(): Boolean = true
    override fun getLocalStoragePath(): String = Path.of(".").toAbsolutePath().toString()
    override fun isLocalStorageAvailable(): Boolean = true
}
