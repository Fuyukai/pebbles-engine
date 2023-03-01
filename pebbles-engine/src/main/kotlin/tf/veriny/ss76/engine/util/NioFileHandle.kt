/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.StreamUtils
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.streams.asSequence

/**
 * Implements file handles on top of NIO.
 */
@Suppress("TooManyFunctions", "SpreadOperator") // go away detekt, this is a subclass
public class NioFileHandle(
    public val path: Path,
    public val fileType: FileType,
) : FileHandle() {
    /**
     * Returns the raw path for this file.
     */
    override fun path(): String {
        return path.toAbsolutePath().toString().replace("\\", "/")
    }

    /**
     * Gets the raw name of this file.
     */
    override fun name(): String {
        return path.fileName.toString()
    }

    /**
     * Returns the file extension (without the dot) or an empty string if the file name doesn't
     * contain a dot.
     */
    override fun extension(): String {
        return path.extension
    }

    /**
     * Returns the name of the file, without parent paths or the extension.
     */
    override fun nameWithoutExtension(): String {
        return path.nameWithoutExtension // thanks kotlin
    }

    /**
     * Returns the path and filename without the extension, e.g. dir/dir2/file.png -> dir/dir2/file.
     * Backward slashes will be returned as forward slashes.
     */
    override fun pathWithoutExtension(): String {
        return path.toAbsolutePath()
            .toString()
            .substringBeforeLast('.')
            .replace("\\", "/")
    }

    /**
     * The type of this file.
     */
    override fun type(): FileType {
        return fileType
    }

    /**
     * Converts the underlying Path into a [File]. This will not work for zipped files.
     */
    override fun file(): File {
        return path.toFile()
    }

    /**
     * Returns a stream for reading this file as bytes.
     */
    override fun read(): InputStream {
        return path.inputStream(StandardOpenOption.READ)
    }

    /**
     * Returns a buffered stream for reading this file as bytes.
     */
    override fun read(bufferSize: Int): BufferedInputStream {
        return BufferedInputStream(read(), bufferSize)
    }

    /**
     * Returns a reader for reading this file in UTF-8.
     */
    override fun reader(): Reader {
        return path.reader(Charsets.UTF_8)
    }

    /**
     * Returns a reader for reading this file as characters, in the specified charset.
     */
    override fun reader(charset: String): Reader {
        return path.reader(Charset.forName(charset))
    }

    /**
     * Returns a buffered reader for reading this file in UTF-8.
     */
    override fun reader(bufferSize: Int): BufferedReader {
        return path.bufferedReader(
            charset = Charsets.UTF_8,
            bufferSize = bufferSize,
            options = arrayOf(StandardOpenOption.READ)
        )
    }

    /**
     * Returns a buffered reader for reading this file as characters, in the specified charset.
     */
    override fun reader(bufferSize: Int, charset: String?): BufferedReader {
        return path.bufferedReader(
            charset = Charset.forName(charset),
            bufferSize = bufferSize,
            options = arrayOf(StandardOpenOption.READ)
        )
    }

    /**
     * Reads the entire file into a string using UTF-8.
     */
    override fun readString(): String {
        return path.readText(Charsets.UTF_8)
    }

    /**
     * Reads the entire file into a string using the specified charset.
     */
    override fun readString(charset: String?): String {
        return path.readText(Charset.forName(charset))
    }

    /**
     * Reads the entire file into a [ByteArray].
     */
    override fun readBytes(): ByteArray {
        return path.readBytes()
    }

    /**
     * Reads the entire file into the specified byte array.
     *
     * @param bytes The array to load the file into
     * @param offset The offset to start writing bytes
     * @param size The number of bytes to read, see [.length]
     * @return The number of bytes read from the file.
     */
    override fun readBytes(bytes: ByteArray, offset: Int, size: Int): Int {
        return read().use {
            it.read(bytes, offset, size)
        }
    }

    /**
     * Creates a memory mapped [ByteBuffer] for this file.
     */
    override fun map(): ByteBuffer {
        return map(FileChannel.MapMode.READ_WRITE)
    }

    /**
     * Creates a memory mapped [ByteBuffer] for this file, with the specified mode.
     */
    override fun map(mode: FileChannel.MapMode): ByteBuffer {
        // required by e.g. freetypefontgenerator
        if (type == FileType.Classpath) throw GdxRuntimeException("cannot map classpath file")

        val openOption = when (mode) { // similar mechanism to real FileHandle
            FileChannel.MapMode.READ_ONLY -> StandardOpenOption.READ
            FileChannel.MapMode.PRIVATE, FileChannel.MapMode.READ_WRITE -> StandardOpenOption.WRITE
            else -> error("THis can never happen")
        }
        try {
            val channel = FileChannel.open(path, openOption)
            return channel.map(mode, 0, channel.size())
        } catch (e: Exception) {
            throw GdxRuntimeException("failed to map file", e)
        }
    }

    private fun getOptions(append: Boolean): Array<StandardOpenOption> {
        return if (append) {
            arrayOf(StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
        } else {
            arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE)
        }
    }

    /**
     * Returns a stream for writing to this file. Parent directories will be created if necessary.
     *
     * @param append If true, file will not be overwritten.
     */
    override fun write(append: Boolean): OutputStream {
        path.createDirectories()
        return path.outputStream(*getOptions(append))
    }

    /**
     * Returns a buffered stream for writing to this file. Parent directories will be created if
     * necessary.
     *
     * @param append If true, file will not be overwritten.
     * @param bufferSize The size of the buffer.
     */
    override fun write(append: Boolean, bufferSize: Int): OutputStream {
        return BufferedOutputStream(write(append), bufferSize)
    }

    /**
     * Reads the remaining bytes from the specified stream and writes them to this file.
     * Will automatically close both streams, and parent directories will be created if necessary.
     *
     * @param append If true, file will not be overwritten.
     */
    override fun write(input: InputStream?, append: Boolean) {
        val os = write(append = append)
        os.use {
            input.use {
                StreamUtils.copyStream(input, os)
            }
        }
    }

    /**
     * Returns a writer for writing to this file using UTF-8. Parent directories will be created
     * if necessary.
     */
    override fun writer(append: Boolean): Writer {
        path.createDirectories()
        return path.writer(Charsets.UTF_8, *getOptions(append))
    }

    /** Returns a writer for writing to this file. Parent directories will be created if necessary.
     *
     * @param append If true, file will not be overwritten.
     * @param charset The charset to use when writing.
     */
    override fun writer(append: Boolean, charset: String): Writer {
        path.createDirectories()
        return path.writer(Charset.forName(charset), *getOptions(append))
    }

    /**
     * Writes the specified string to the file using UTF-8. Parent directories will be created if
     * necessary.
     *
     * @param append If true, file will not be overwritten.
     */
    override fun writeString(string: String?, append: Boolean) {
        path.createDirectories()
        // no writeString ext?
        Files.writeString(path, string, Charsets.UTF_8, *getOptions(append))
    }

    /**
     * Writes the specified string to the file using the specified charset.
     * Parent directories will be created if necessary.
     *
     * @param append If true, file will not be overwritten.
     * @param charset The charset to use when writing.
     */
    override fun writeString(string: String?, append: Boolean, charset: String?) {
        path.createDirectories()
        Files.writeString(path, string, Charset.forName(charset), *getOptions(append))
    }

    /**
     * Writes the specified bytes to the file. Parent directories will be created if necessary.
     *
     * @param append If true, file will not be overwritten.
     */
    override fun writeBytes(bytes: ByteArray, append: Boolean) {
        path.createDirectories()
        path.writeBytes(bytes, *getOptions(append))
    }

    /**
     * Writes the specified bytes to the file. Parent directories will be created if necessary.
     *
     * @param append If true, file will not be overwritten.
     */
    override fun writeBytes(bytes: ByteArray, offset: Int, length: Int, append: Boolean) {
        path.createDirectories()
        write(append = append).use {
            it.write(bytes, offset, length)
        }
    }

    /**
     * Returns the paths to the children of this directory. Returns an empty array if this file
     * handle represents a file and not a directory.
     */
    override fun list(): Array<NioFileHandle> {
        // grr array
        if (!path.isDirectory()) return arrayOf()
        return path.listDirectoryEntries().map { NioFileHandle(it, fileType) }.toTypedArray()
    }

    // none of these filter methods are ever used in the libgdx code. ignore them and use nio
    // directly
    override fun list(filter: FileFilter?): Array<FileHandle> {
        // this is never ONCE used. use listDirectoryEntries().filter().
        throw NotImplementedError("unused")
    }

    override fun list(filter: FilenameFilter?): Array<FileHandle> {
        throw NotImplementedError("unused")
    }

    override fun list(suffix: String?): Array<FileHandle> {
        throw NotImplementedError("unused")
    }

    // no directory limitation as NIO files work just fine
    /**
     * Returns if this path is a directory or not.
     */
    override fun isDirectory(): Boolean {
        return path.isDirectory()
    }

    /**
     * Returns a handle to the child with the specified name.
     */
    override fun child(name: String): NioFileHandle {
        return NioFileHandle(path.resolve(name), fileType)
    }

    /**
     * Returns a handle to the sibling with the specified name.
     */
    override fun sibling(name: String): NioFileHandle {
        return NioFileHandle(path.resolveSibling(name), fileType)
    }

    /**
     * Returns the parent file of this file.
     */
    override fun parent(): FileHandle {
        return NioFileHandle(path.parent, fileType)
    }

    /**
     * Creates the directory referred to by this file, as well as any parent files.
     */
    override fun mkdirs() {
        path.createDirectories()
    }

    /**
     * Returns if this file exists.
     */
    override fun exists(): Boolean {
        return path.exists()
    }

    /**
     * Deletes this file (not folder), and returns if it existed.
     */
    override fun delete(): Boolean {
        return path.deleteIfExists()
    }

    /**
     * Deletes this file or directory and all children, recursively.
     */
    override fun deleteDirectory(): Boolean {
        if (!path.exists()) return false

        Files.walk(path)
            .asSequence() // java -> kotlin
            .sortedWith(Comparator.reverseOrder())
            .forEach { it.deleteIfExists() }

        return true
    }

    override fun emptyDirectory() {
        Files.walk(path)
            .asSequence() // java -> kotlin
            .sortedWith(Comparator.reverseOrder())
            .filter { it != path }
            .forEach { it.deleteIfExists() }
    }

    override fun emptyDirectory(preserveTree: Boolean) {
        // fuck off
        Files.walk(path)
            .asSequence() // java -> kotlin
            .sortedWith(Comparator.reverseOrder())
            .filter { it != path && !it.isDirectory() }
            .forEach { it.deleteIfExists() }
    }

    // not implemented, unused in libgdx code
    override fun copyTo(dest: FileHandle) {
        throw NotImplementedError("unused")
    }

    override fun moveTo(dest: FileHandle) {
        throw NotImplementedError("unused")
    }

    /**
     * Returns the length of this file, or 0 if it doesn't have a length.
     */
    @Suppress("SwallowedException")
    override fun length(): Long {
        return try {
            path.fileSize()
        } catch (e: IOException) {
            0L
        }
    }

    /**
     * Returns the last modified time for this file, in milliseconds.
     */
    @Suppress("SwallowedException")
    override fun lastModified(): Long {
        return try {
            return path.getLastModifiedTime().toMillis()
        } catch (e: IOException) {
            0L
        }
    }
}
