/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import com.badlogic.gdx.Audio
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AssetLoader
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import ktx.assets.getAsset
import tf.veriny.ss76.engine.MusicManager.Companion.NO_MUSIC
import tf.veriny.ss76.engine.util.EktFiles
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.reflect.KClass

/**
 * Wraps a libgdx asset manager with SS76-specific knowledge.
 */
public class EngineAssetManager {
    public class AssetLoadException(
        public val name: String,
        message: String = "Failed to load asset 'name'",
        cause: Throwable? = null
    ) : Exception(message, cause)

    public data class AssetType(
        public val name: String,
        public val pathPrefix: String,
        public val extension: String,
        public val klass: KClass<*>,
    )

    private val gdxAssets = AssetManager(EktFiles.RESOLVER)
    private val assetTypes = mutableMapOf<KClass<*>, AssetType>()

    /**
     * Adds a new asset type to the asset manager, for the specified class and loader.
     */
    public fun <T : Any, P : AssetLoaderParameters<T>> addAssetType(
        type: AssetType,
        loader: AssetLoader<T, P>
    ) {
        assetTypes[type.klass] = type
        gdxAssets.setLoader(type.klass.java as Class<T>, loader)
    }

    /**
     * Gets a single arbitrary asset. Designed to be used by extension functions.
     */
    public fun <T : Any> getArbitraryAsset(
        type: KClass<T>,
        name: String
    ): T {
        val loader = assetTypes[type]!!
        return gdxAssets.get("data/${loader.pathPrefix}/$name.${loader.extension}", type.java)
    }


    /** Gets a single background. */
    public fun getBackground(name: String): Texture {
        return gdxAssets.getAsset("data/Cg/Bgd/$name.png")
    }

    public fun getAtlas(name: String): TextureAtlas {
        return gdxAssets.getAsset("data/Cg/Obd/$name.atlas")
    }

    public fun hasMusicFile(name: String): Boolean {
        val path = "data/Sound/se/$name.ogg"
        return gdxAssets.isLoaded(path)
    }

    public fun tryGetMusicFile(name: String): Music? {
        val path = "data/Sound/se/$name.ogg"
        val hasAsset = gdxAssets.isLoaded(path)
        if (!hasAsset) return null
        return gdxAssets.getAsset(path)
    }

    // TODO: refactor this to be more dynamic
    private fun loadAsset(path: Path) {
        // le libgdx
        when {
            path.startsWith("Cg/Bgd") && path.extension == "png" -> {
                println("LOAD (BGD): $path")
                gdxAssets.load("data/$path", Texture::class.java)
            }

            path.startsWith("Sound/se") && path.extension == "ogg" -> {
                if (NO_MUSIC) return
                println("LOAD (SE): $path")
                gdxAssets.load("data/$path", Music::class.java)
            }

            path.startsWith("Sound/adx") && path.extension == "ogg" -> {
                println("LOAD (ADX): $path")
                gdxAssets.load("data/$path", Audio::class.java)
            }

            path.startsWith("Cg/Obd") && path.extension == "png" -> {}
            path.startsWith("Cg/Obd") && path.extension == "atlas" -> {
                println("LOAD (OBJECT): $path")
                gdxAssets.load("data/$path", TextureAtlas::class.java)
            }

            else -> {
                for (type in assetTypes.values) {
                    if (path.startsWith(type.pathPrefix) && path.extension == type.extension) {
                        println("LOAD (${type.name}): $path")
                        gdxAssets.load("data/$path", type.klass.java)
                        return
                    }
                }

                return println("unknown asset: $path (ext: ${path.extension})")
            }
        }
    }

    /**
     * Automatically loads all known assets into memory.
     */
    @OptIn(ExperimentalPathApi::class)
    public fun autoload() {
        val assetsDir = javaClass.classLoader.getResource("data")?.toURI()
                        ?: throw AssetLoadException("data", "Can't find any data directory!")

        if (assetsDir.scheme == "file") {
            val path = assetsDir.toPath()
            path.walk(PathWalkOption.BREADTH_FIRST).forEach { p ->
                val lowerPath = p.relativeTo(path)
                loadAsset(lowerPath)
            }
        } else {
            val split = assetsDir.toString().split("!")

            FileSystems.newFileSystem(URI.create(split[0]), mutableMapOf<String, String>()).use {
                val path = it.getPath(split[1])
                path.walk(PathWalkOption.BREADTH_FIRST).forEach { p ->
                    val lowerPath = p.relativeTo(path)
                    loadAsset(lowerPath)
                }
            }
        }

        gdxAssets.finishLoading()
    }
}
