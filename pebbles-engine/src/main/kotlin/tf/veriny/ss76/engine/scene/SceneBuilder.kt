/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import com.badlogic.gdx.graphics.Color
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.*
import tf.veriny.ss76.engine.adv.ADVSubRenderer

private val PUSH_REGEX = "^(?:push-scene-|ps-)(.*)".toRegex()
private val CHANGE_REGEX = "^(?:change-scene-|cs-)(.*)".toRegex()

/**
 * Returned by page builders.
 */
@JvmInline
public value class RegisteredPage(public val idx: Int)

/**
 * A single builder for a single page.
 */
public class PageBuilder(
    private val page: StringBuilder,
    private val addButton: (Button) -> Unit
) {
    private fun ensureBlankChar() {
        val lastChar = page.lastOrNull()
        if (lastChar != null && lastChar != ' ' && lastChar != '\n') {
            page.append(' ')
        }
    }

    /**
     * Appends raw text to this page.
     */
    public fun raw(content: String) {
        page.append(content)
    }

    /**
     * Clears the current page.
     */
    public fun clear() {
        page.clear()
    }

    /**
     * Adds a new line of text to this page. This will be automatically split and formatted
     * according to the various formatting characters.
     */
    public fun line(data: String, addNewline: Boolean = true) {
        page.append(data)
        if (addNewline) page.append('\n')
    }

    /**
     * Adds a line of text without a trailing newline. This is equiv to
     * ``line(data, addNewline = false)``.
     */
    public fun nnline(data: String) {
        line(data, addNewline = false)
    }

    /**
     * Adds a line of text with linger frames.
     */
    public fun lline(
        data: String,
        lingerFrames: Int = -1,
        addNewline: Boolean = true
    ) {
        page.append(data)
        ensureBlankChar()

        if (lingerFrames > 1) {
            page.append(":linger:$lingerFrames")
        } else {
            page.append(":linger:")
        }

        if (addNewline) page.append('\n')
    }

    /**
     * Adds a line of text that obeys dialogue rules (i.e. is padded by 6 characters).
     */
    public fun dline(
        data: String, addNewline: Boolean = true, linger: Boolean = true, lingerFrames: Int = 60
    ) {
        ensureBlankChar()
        var realText = ":push:¬dialogue¬ $data :pop: "

        if (linger) realText += ":linger:$lingerFrames"

        line(realText, addNewline = false)
        if (addNewline) {
            newline(2)
        }
    }

    /**
     * Adds a newline.
     */
    public fun newline(count: Int = 1) {
        repeat(count) { page.append('\n') }
    }

    // convenience
    /**
     * Adds a button that changes the current scene.
     */
    public fun changeSceneButton(
        sceneId: String,
        text: String,
        eventFlag: String? = null,
        eventValue: Int = 1,
    ) {
        ensureBlankChar()
        val buttonName = if (eventFlag != null) {
            "change-scene-$sceneId-$eventFlag"
        } else "change-scene-$sceneId"
        val realText = ":push:@linked@`$buttonName` $text :pop: "

        line(realText, addNewline = false)
        addButton(ChangeSceneButton(buttonName, sceneId, eventFlag, eventValue))
    }

    public fun nextSceneButton(
        sceneId: String,
        text: String,
        eventFlag: String? = null,
        eventValue: Int = 1,
    ) {
        ensureBlankChar()
        val buttonName = if (eventFlag != null) {
            "change-scene-$sceneId-$eventFlag"
        } else "change-scene-$sceneId"
        val realText = ":push:@salmon@`$buttonName` $text :pop: "

        line(realText, addNewline = false)
        addButton(ChangeSceneButton(buttonName, sceneId, eventFlag, eventValue))
    }

    /**
     * Adds a button that pushes a new scene onto the stack.
     */
    public fun pushSceneButton(
        sceneId: String,
        text: String,
        eventFlag: String? = null,
        eventValue: Int = 1,
    ) {
        ensureBlankChar()
        val buttonName = "push-scene-$sceneId"
        val realText = ":push:@linked@`push-scene-$sceneId` $text :pop: "
        line(realText, addNewline = false)

        addButton(PushSceneButton(buttonName, sceneId, eventFlag, eventValue))
    }

    /**
     * Adds a green back button.
     */
    public fun backButton(text: String = "« Back") {
        val realText = ":push:@green@`back-button` $text :pop: "
        line(realText)

        addButton(BackButton)
    }

}

public class PageTemplate(
    private val builder: SceneDefinitionBuilder,
    private val previousContent: String
) {
    /**
     * Extends this template into another template.
     */
    public fun moreTemplate(block: PageBuilder.() -> Unit): PageTemplate {
        return builder.template {
            raw(previousContent)
            block()
        }
    }

    /**
     * Creates this page, without adding additional content.
     */
    public fun create(): RegisteredPage {
        return builder.page { raw(previousContent) }
    }

    /**
     * Extends this template, without marking the previous text as instant.
     */
    public fun standalone(block: PageBuilder.() -> Unit): RegisteredPage {
        return builder.page {
            raw(previousContent)
            this.block()
        }
    }

    /**
     * Extends this template, marking the previous text as instant.
     */
    public fun extra(chomp: Boolean = true, block: PageBuilder.() -> Unit): RegisteredPage {
        return builder.page {
            raw(":newline-linger:false :push:¬instant¬ ")
            raw(previousContent)
            raw(" :pop: :newline-linger:true ")
            if (chomp) raw(":chomp:")

            this.block()
        }
    }

}

/**
 * Builder helper for creating new scene definitions.
 */
public class SceneDefinitionBuilder(
    private val sceneId: String,
) {
    public val pages: MutableList<StringBuilder> = mutableListOf()

    @PublishedApi
    internal val buttons: MutableMap<String, Button> = mutableMapOf()

    private val onLoadHandlers: MutableList<(SceneState) -> Unit> = mutableListOf()

    /**
     * The modifiers for this scene. You should set your options with ``modifiers = modifiers.copy(...)``.
     */
    public var modifiers: SceneModifiers = SceneModifiers()

    /** The ADV mode sub-renderer for this scene. */
    public var advRenderer: ADVSubRenderer? = null

    /** The default frames per word to use, when not  */
    public var defaultFramesPerWord: Int = DEFAULT_FRAMES_PER_WORD

    // == modifier functions == //
    public fun enablePagination(): SceneDefinitionBuilder {
        modifiers = modifiers.copy(drawPageButtons = true)
        return this
    }

    public fun fadeIn(): SceneDefinitionBuilder {
        modifiers = modifiers.copy(causesFadeIn = true)
        return this
    }

    public fun backgroundName(name: String): SceneDefinitionBuilder {
        modifiers = modifiers.copy(backgroundName = name)
        return this
    }

    public fun textOnly(background: Color = Color.BLACK): SceneDefinitionBuilder {
        modifiers = modifiers.copy(backgroundColour = background, textOnlyMode = true)
        return this
    }

    public fun alwaysAllowTextSkip(): SceneDefinitionBuilder {
        modifiers = modifiers.copy(alwaysAllowTextSkip = true)
        return this
    }

    /** Plays the specified music when this scene is loaded. */
    public fun playMusicTrack(track: String) {
        onLoad { it.engineState.musicManager.startTrack(track) }
    }

    /** Stops the music when this scene is loaded. */
    public fun stopMusic() {
        onLoad { it.engineState.musicManager.stop() }
    }

    // == Flag helpers == //
    /** Sets an event flag when this scene is loaded. */
    public fun setFlagOnLoad(flag: String, value: Int = 1) {
        onLoad { it.engineState.eventFlagsManager.set(flag, value) }
    }

    /** Increments an event flag when this scene is loaded. */
    public fun incrementFlagOnLoad(flag: String) {
        onLoad { it.engineState.eventFlagsManager.increment(flag) }
    }


    /** Changes to the specified page if all of the provided flags are set. */
    public fun changePageIfAllFlagsSet(pageIdx: RegisteredPage, vararg flags: String, value: Int = 1) {
        onLoad {
            var failure = false

            for (flag in flags) {
                if (it.engineState.eventFlagsManager.getValue(flag) != value) {
                    failure = true
                    break
                }
            }

            if (!failure) it.pageIdx = pageIdx.idx
        }
    }

    /** Changes to the specified page if the specified flag is set. */
    public fun changePageIfFlag(pageIdx: RegisteredPage, flag: String, value: Int) {
        changePageIfAllFlagsSet(pageIdx, flag, value = value)
    }

    /**
     * Registers a function to be ran on load.
     */
    public fun onLoad(block: (SceneState) -> Unit) {
        onLoadHandlers += block
    }

    /**
     * Adds a generic button to the current scene. This can be referenced with backtick syntax
     * on nodes.
     */
    public fun addButton(
        button: Button,
    ) {
        buttons[button.name] = button
    }

    public fun addButton(name: String, action: (state: EngineState) -> Unit) {
        val button = object : Button {
            override val name: String = name
            override fun run(state: EngineState) {
                action(state)
            }
        }
        buttons[name] = button
    }

    /**
     * Creates a new page.
     */
    public inline fun page(block: PageBuilder.() -> Unit): RegisteredPage {
        val page = StringBuilder()
        val builder = PageBuilder(page, this::addButton)
        builder.block()
        pages.add(page)
        return RegisteredPage(pages.size - 1)
    }

    /**
     * Creates a new page template.
     */
    public inline fun template(block: PageBuilder.() -> Unit): PageTemplate {
        val page = StringBuilder()
        val builder = PageBuilder(page, this::addButton)
        builder.block()
        return PageTemplate(this, page.toString())
    }

    /**
     * Creates a definition from a page.
     */
    private fun createDefinitionFromPage(page: StringBuilder): List<TextualNode> {
        val pageFullString = page.toString()
        val nodes = try {
            tokenifyScene(pageFullString, defaultFramesPerWord = defaultFramesPerWord, v = false)
        } catch (e: Exception) {
            throw SS76EngineInternalError(
                "Caught error trying to tokenize:\n$pageFullString",
                e
            )
        }

        // auto-create missing buttons
        val missing = nodes.filter { it.buttonId != null && !buttons.contains(it.buttonId) }

        for (node in missing) {
            val buttonName = node.buttonId!!

            val pushMatch = PUSH_REGEX.matchEntire(buttonName)
            if (pushMatch != null) {
                val sceneId = pushMatch.groups[1]!!.value
                val button = PushSceneButton(buttonName, sceneId)
                buttons[buttonName] = button
                continue
            }

            val csMatch = CHANGE_REGEX.matchEntire(buttonName)
            if (csMatch != null) {
                val sceneId = csMatch.groups[1]!!.value
                val button = ChangeSceneButton(buttonName, sceneId)
                buttons[buttonName] = button
                continue
            }

            throw IllegalArgumentException("Missing definition for button $buttonName")
        }

        return nodes
    }

    /**
     * Creates the scene definition for this builder.
     */
    public fun createDefinition(): SceneDefinition {
        val pages = mutableListOf<List<TextualNode>>()
        for (page in this.pages) {
            val nodes = createDefinitionFromPage(page)
            pages.add(nodes)
        }

        return SceneDefinition(
            sceneId, buttons, pages, originalPages = this.pages.map { it.toString() },
            onLoadHandlers = this.onLoadHandlers,
            advSubRenderer = advRenderer,
            modifiers = modifiers
        )
    }
}

/**
 * Builder for a scene sequence.
 */
public class SceneSequenceBuilder(
    private val sceneManager: SceneManager,
    public val idPrefix: String,
) {

    public var defaultModifiers: SceneModifiers = SceneModifiers()

    /** The current ADV renderer. */
    public var advRenderer: ADVSubRenderer? = null

    /**
     * Creates and registers a new scene.
     */
    public fun createAndRegisterScene(
        sceneId: String, block: SceneDefinitionBuilder.() -> Unit
    ): SceneDefinition {
        val builder = SceneDefinitionBuilder(idPrefix + sceneId)
        builder.advRenderer = advRenderer
        builder.modifiers = defaultModifiers.copy()
        builder.block()

        val definition = builder.createDefinition()
        sceneManager.registerScene(definition)
        return definition
    }

    /**
     * Creates and registers a new, single-page scene.
     */
    public inline fun createAndRegisterOnePageScene(
        sceneId: String,
        background: String? = null,
        crossinline block: PageBuilder.() -> Unit,
    ): SceneDefinition {
        return createAndRegisterScene(sceneId) {
            background?.let { backgroundName(it) }
            page(block)
        }
    }
}

/**
 * Creates a sequence of scenes which share certain properties.
 */
public inline fun SceneManager.sceneSequence(
    idPrefix: String = "", block: SceneSequenceBuilder.() -> Unit
) {
    val builder = SceneSequenceBuilder(this, idPrefix)
    builder.block()
}

/**
 * Creates a new scene.
 */
public inline fun createScene(
    sceneId: String,
    block: SceneDefinitionBuilder.() -> Unit,
): SceneDefinition {
    val builder = SceneDefinitionBuilder(sceneId)
    builder.block()

    return builder.createDefinition()
}

/**
 * Creates a new scene and registers it with the scene manager.
 */
public inline fun SceneManager.createAndRegisterScene(
    sceneId: String, block: SceneDefinitionBuilder.() -> Unit
): SceneDefinition {
    val scene = createScene(sceneId, block = block)
    registerScene(scene)
    return scene
}

public inline fun SceneManager.createAndRegisterOnePageScene(
    sceneId: String,
    background: String? = null,
    block: PageBuilder.() -> Unit,
): SceneDefinition {
    return createAndRegisterScene(sceneId) {
        background?.let { backgroundName(it) }
        page(block)
    }
}
