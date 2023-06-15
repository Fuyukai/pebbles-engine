/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene.builder

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.ChangeSceneButton
import tf.veriny.ss76.engine.PushSceneButton
import tf.veriny.ss76.engine.psm.PsmIncludedFragment
import tf.veriny.ss76.engine.psm.PsmSceneFragment
import tf.veriny.ss76.engine.psm.UnbakedScene
import tf.veriny.ss76.engine.scene.OnLoadHandler
import tf.veriny.ss76.engine.scene.SceneModifiers

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
    override var modifiers: SceneModifiers = SceneModifiers(),
) : HasModifiers {
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

        /**
         * Adds a new raw fragment that is only shown if the provided flag is set.
         */
        public fun addFragmentIfFlag(
            id: String,
            instant: Boolean = false,
            flag: String,
            flagValue: Int
        ) {
            addFragment(id, instant) { it.eventFlagsManager.getValue(flag) == flagValue }
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

    // specific button helpers
    /**
     * Adds a new scene-change button (with the ID `cs-<sceneId>`) that sets the provided [flagId]
     * to [flagValue] on click.
     *
     * This is only required if you are setting flags.
     */
    public fun addSceneChangeButton(sceneId: String, flagId: String? = null, flagValue: Int = 0) {
        val buttonId = "cs-${sceneId}"
        val button = ChangeSceneButton(buttonId, sceneId, setFlag = flagId, eventValue = flagValue)
        addButton(button)
    }

    /**
     * Adds a new push-scene button (with the ID `ps-<sceneId>`) that sets the provided [flagId]
     * to [flagValue] on click.
     *
     * This is only required if you are setting flags. Otherwise, buttons with the id
     * ``ps-<sceneId>`` are automatically generated.
     */
    public fun addPushChangeButton(sceneId: String, flagId: String? = null, flagValue: Int = 0) {
        val buttonId = "cs-${sceneId}"
        val button = PushSceneButton(buttonId, sceneId, setFlag = flagId, eventValue = flagValue)
        addButton(button)
    }

    /**
     * Adds a new button to this scene.
     */
    public inline fun button(
        id: String, linkedId: String? = null,
        crossinline action: (EngineState) -> Unit
    ): Button {
        return object : Button {
            override val name: String get() = id
            override val linkedId: String? get() = linkedId
            override fun run(state: EngineState) {
                return action.invoke(state)
            }
        }.also { addButton(it) }
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
