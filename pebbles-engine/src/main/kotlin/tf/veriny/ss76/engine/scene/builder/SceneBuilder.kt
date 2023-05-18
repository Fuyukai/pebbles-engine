package tf.veriny.ss76.engine.scene.builder

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.psm.PsmIncludedFragment
import tf.veriny.ss76.engine.psm.PsmSceneFragment
import tf.veriny.ss76.engine.psm.UnbakedScene
import tf.veriny.ss76.engine.scene.OnLoadHandler
import tf.veriny.ss76.engine.scene.SceneModifiers
import tf.veriny.ss76.engine.scene.onLoad

/**
 * Return type of [SceneBuilder.page].
 */
@JvmInline
public value class PageIndex(public val index: Int)

/**
 * Class used for building scenes.
 */
public class SceneBuilder
@PublishedApi internal constructor(
    private val state: EngineState,
    private val sceneId: String,
    /** The scene modifiers for this scene. May be copied and edited. */
    public var modifiers: SceneModifiers = SceneModifiers(),
) {
    public inner class PageBuilder {
        internal val includedFragments: MutableList<PsmIncludedFragment> = mutableListOf()

        /**
         * Adds a new scene fragment to this scene builder.
         */
        public fun addFragment(
            id: String,
            instant: Boolean = false,
            condition: ((EngineState) -> Boolean)? = null,
        ) {
            val fragment = state.psmLoader[id]
            includedFragments.add(PsmIncludedFragment(fragment, instant, condition))
        }

        /**
         * Adds a new raw fragment with statically defined PSM text.
         */
        public fun addRawFragment(
            content: String,
            instant: Boolean = false,
            condition: ((EngineState) -> Boolean)? = null
        ) {
            val rawFragment = PsmSceneFragment(content)
            includedFragments.add(PsmIncludedFragment(rawFragment, instant, condition))
        }
    }

    internal val onLoad = mutableListOf<OnLoadHandler>()
    internal val pages: MutableList<List<PsmIncludedFragment>> = mutableListOf()
    internal val buttons: MutableList<Button> = mutableListOf()

    /**
     * Adds a new function that will be called when a scene is loaded.
     */
    public fun onLoad(fn: OnLoadHandler) {
        onLoad.add(fn)
    }

    /**
     * Creates a new [PageBuilder] and passes it to the specified function, building a new page,.
     */
    public fun page(block: PageBuilder.() -> Unit): PageIndex {
        val builder = PageBuilder()
        builder.block()

        val index = pages.size
        pages.add(builder.includedFragments)
        return PageIndex(index)
    }

    /**
     * Adds a new single-fragment page to this scene.
     */
    public fun page(id: String): PageIndex {
        return page { addFragment(id) }
    }

    /**
     * Adds a new button to this scene builder.
     */
    public fun addButton(button: Button) {
        buttons.add(button)
    }

    internal fun get(): UnbakedScene {
        return UnbakedScene(
            sceneId = sceneId,
            pages = pages,
            buttons = buttons,
            onLoadHandlers = onLoad,
            modifiers = modifiers,
        )
    }
}
