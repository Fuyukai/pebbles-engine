package tf.veriny.ss76.engine.psm

import tf.veriny.ss76.engine.util.EktFiles
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText

private val REGEXP = "----- (.*)\\n".toRegex()

/**
 * Handles loading PSM fragment bundles.
 */
public class PsmFragmentManager(public val languageCode: String) {
    private val fragments = mutableMapOf<String, PsmSceneFragment>()

    public operator fun get(id: String): PsmSceneFragment {
        return fragments[id] ?: error("No such PSM fragment '$id'")
    }

    init {
        doLoadPsmBundle("engine/builtins/${languageCode}/built-in-locale.psmf")
    }

    private fun doLoadPsmBundle(rawPath: String) {
        val realPath = EktFiles.RESOLVER.getPath(rawPath)!!
        val fullBundleText = realPath.readText(Charsets.UTF_8)
        val matches = REGEXP.findAll(fullBundleText).toList()
        if (matches.isEmpty()) return

        println("PSM bundle contains ${matches.size} scenes")

        for (i in matches.indices) {
            val firstMatch = matches[i]
            val nextMatch = matches.getOrNull(i + 1)

            val sceneId = firstMatch.groups[1]!!.value
            val sceneBodyBeginning = firstMatch.range.last
            val sceneBodyEnding = nextMatch?.range?.first ?: fullBundleText.length
            val sceneBody = fullBundleText.slice(sceneBodyBeginning until sceneBodyEnding)
            fragments[sceneId] = PsmSceneFragment(sceneBody)
        }
    }

    /**
     * Loads all of the PSM fragments in the assets directories.
     */
    public fun loadPsmBundle(path: Path) {
        val language = path.getName(1).name
        // skip files from other languages
        if (language != this.languageCode) {
            return
        }

        doLoadPsmBundle("data/${path}")
    }
}