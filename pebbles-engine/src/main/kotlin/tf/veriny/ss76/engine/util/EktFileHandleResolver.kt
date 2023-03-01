/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

import com.badlogic.gdx.Files
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle
import com.badlogic.gdx.files.FileHandle
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * A file handle resolver that produces NIO files.
 */
public class EktFileHandleResolver : FileHandleResolver {
    /**
     * Resolves a [Path] on the classpath from the string name.
     */
    public fun resolveClasspath(path: String): Path? {
        // NO leading slash, because classloader works from root of the classpath anyway
        val file = EktFileHandleResolver::class.java.classLoader.getResource(path)
                   ?: return null
        val uri = file.toURI()
        return Path.of(uri)
    }

    /**
     * Resolves a [Path] either on the classpath or from a ``./assets`` directory
     */
    public fun getPath(path: String): Path? {
        // why???
        val p = Path.of(path)
        if (p.isAbsolute) {
            return p
        }

        val assetsDir = Path.of("./assets")
        if (assetsDir.exists()) {
            val potential = assetsDir.resolve(path)
            if (potential.exists()) {
                return potential
            }
        }

        return resolveClasspath(path)
    }

    override fun resolve(fileName: String): FileHandle {
        val p = getPath(fileName)
        return if (p != null) {
            NioFileHandle(p, Files.FileType.Internal)
        } else {
            // let the default stupid logic handle it
            Lwjgl3FileHandle(fileName, Files.FileType.Internal)
        }
    }
}
