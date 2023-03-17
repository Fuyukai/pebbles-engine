/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.nvl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import squidpony.squidmath.MiniMover64RNG
import tf.veriny.ss76.engine.SS76EngineInternalError
import tf.veriny.ss76.engine.VnButtonManager
import tf.veriny.ss76.engine.font.Font
import tf.veriny.ss76.engine.scene.SceneState
import tf.veriny.ss76.engine.scene.TextualNode
import tf.veriny.ss76.engine.scene.TextualNode.Effect.RESET
import tf.veriny.ss76.engine.screen.FadeInScreen
import tf.veriny.ss76.engine.util.RandomWrapper
import tf.veriny.ss76.mojibakify
import tf.veriny.ss76.randomChar
import java.security.SecureRandom
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * The renderer for NVL-based scenes.
 */
public class NVLRendererV3(
    private val state: SceneState,
    private val viewport: Viewport,
    private val camera: OrthographicCamera,
    private val buttons: VnButtonManager,
) : Disposable {
    private companion object {
        private val BACKGROUND_BG = Color(48 / 255f, 48 / 255f, 48 / 255f, 0f)

        // colour for the background box
        private val BOX_COLOUR = Color.BLACK.cpy().also { it.a = 0.90f }

        // padding around the edges that the box is drawn
        private const val BORDER_PADDING = 75f

        // padding around the inside of the box
        private const val INNER_PADDING = 15f
    }

    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()

    // allegedly the fastest rng in the lib.
    private val timedRngRaw = MiniMover64RNG()
    private val timedRng = RandomWrapper(timedRngRaw)
    private val realRngRaw = MiniMover64RNG()
    private val realRng = RandomWrapper(realRngRaw)

    private var currentXOffset: Float = 0f
    private var currentYOffset: Float = 0f

    private val background = state.definition.modifiers.backgroundName?.let {
        state.engineState.assets.getBackground(it)
    }

    init {
        batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        realRngRaw.seed(SecureRandom.getInstanceStrong().nextInt())
    }

    override fun dispose() {
        batch.disposeSafely()
        shapeRenderer.disposeSafely()
    }

    // == Extensions == //
    private inline val TextualNode.font: Font
        get() {
            return state.engineState.fontManager.getFont(fontName)
        }

    private var lastPageIdx = -1
    private var cachedPageButtons: List<TextualNode> = emptyList()

    public fun getPageButtons(): List<TextualNode> {
        if (lastPageIdx != state.pageIdx) {
            val nodes = mutableListOf<TextualNode>()
            for (b in listOf("«", "PREVIOUS")) {
                val node = TextualNode(
                    b, startFrame = 0, endFrame = 0, buttonId = "page-back",
                    colour = Color.SALMON
                )
                nodes.add(node)
            }

            val definition = state.definition
            for (b in "== PAGE ${state.pageIdx + 1}/${definition.pageCount} ==".split(" ")) {
                val node = TextualNode(b, startFrame = 0, endFrame = 0, colour = Color.WHITE)
                nodes.add(node)
            }

            nodes.add(TextualNode(
                "NEXT", startFrame = 0, endFrame = 0, buttonId = "page-next",
                colour = Color.SALMON
            ))
            nodes.add(
                TextualNode(
                    "»", startFrame = 0, endFrame = 0, buttonId = "page-next",
                    colour = Color.SALMON, causesNewline = true
                )
            )

            // blank newline
            nodes.add(TextualNode("", startFrame = 0, endFrame = 0, causesNewline = true))

            cachedPageButtons = nodes
            lastPageIdx = state.pageIdx
        }

        return cachedPageButtons
    }

    /**
     * Renders a single word onto the screen. The X and Y positions are absolute pre-computed
     * coordinates.
     */
    private fun renderWord(
        x: Float, y: Float,
        font: Font, colour: Color?,
        text: String,
        effects: Set<TextualNode.Effect>,
        getButtonRect: Boolean = true,
    ): Rectangle? {
        val sceneModifiers = state.definition.modifiers
        val realColour = when {
            colour != null -> colour
            sceneModifiers.textOnlyMode -> sceneModifiers.textOnlyModeColour
            sceneModifiers.defaultTextColour != null -> sceneModifiers.defaultTextColour
            else -> font.defaultColour
        }

        var realX = x
        var realY = y

        if (TextualNode.Effect.SHAKE in effects) {
            realX += realRng.nextInt(-2, 2)
            realY += realRng.nextInt(-1, 1)
        }

        font.drawWithColour(batch, text, realColour, x, y)

        return if (getButtonRect) {
            val textWidth = font.widthOf(text)
            val textHeight = font.heightOf(text)
            val rect = buttons.getRectangle()

            // we want some extra width so that the spaces between the words count
            // but we want the space closest to the node itself to be the link instead of the
            // whole space directly after it for the case of two links placed right next to
            // each-other.

            val extraWidth = font.characterWidth
            rect.x = realX - extraWidth / 2f
            rect.width = textWidth + extraWidth

            // subtract height as the rect coords point to top-left(?)
            // either way, this fixes it
            rect.y = realY - textHeight
            rect.height = textHeight

            rect
        } else {
            null
        }
    }

    /**
     * Renders a single textual node.
     */
    private fun renderTextualNode(
        frameDataNode: TextualNode, node: TextualNode = frameDataNode
    ) {
        var colour = node.colour
        if (node.colourLinkedToButton) {
            val isGreen = state.engineState.sceneManager.linkHelper.isNodeGreen(state.definition, node)
            colour = if (isGreen) Color.GREEN else Color.RED
        }

        var text = node.text
        var isTruncated = false

        // truncate text appropriate, if needed
        if (state.timer < frameDataNode.endFrame) {
            val totalFrames = (frameDataNode.endFrame - frameDataNode.startFrame).toFloat()
            val framesLeft = (frameDataNode.endFrame - state.timer).toFloat()
            val fraction = 1 - (framesLeft / totalFrames)
            val length = ceil((text.length * fraction)).toInt()
            if (length < text.length) {
                isTruncated = true
                text = text.substring(0..length)
            }
        }

        // text replacement effects
        if (TextualNode.Effect.SHUFNUM in node.effects) {
            val sb = StringBuilder()
            for (char in text) {
                if (char.isDigit()) {
                    sb.append(timedRng.nextInt(0, 9))
                } else {
                    sb.append(char)
                }
            }
            text = sb.toString()
        } else if (TextualNode.Effect.SHUFTXT in node.effects) {
            text = buildString {
                // undo truncation. gretchy said this was a cool eeffect...
                for (c in node.text) {
                    if (c.isDigit() || c.isLetter()) {
                        append(randomChar(timedRng))
                    } else {
                        append(c)
                    }
                }
            }
        }

        if (TextualNode.Effect.MOJIBAKE in node.effects) {
            text = text.mojibakify(timedRng)
        }

        val x = BORDER_PADDING + INNER_PADDING + currentXOffset
        // negatives as y is up, but the y offset goes down.
        val y = viewport.worldHeight - BORDER_PADDING - INNER_PADDING - currentYOffset

        val button = state.definition.buttons[node.buttonId]
        val rect = renderWord(
            x, y, node.font, colour, text, node.effects,
            getButtonRect = !isTruncated && button != null
        )

        if (button != null && rect != null) {
            buttons.addClickableArea(button, rect)
        }
    }

    /**
     * Advances the offsets based on the node to be drawn.
     */
    private fun advanceByNode(node: TextualNode) {
        // don't take partial drawing into account as it'll look really weird.
        val font = node.font
        val width = font.widthOf(node.text)
        val height = font.heightOf(node.text)

        if (node.causesNewline) {
            currentYOffset += height
            currentXOffset = 0f
        } else {
            currentXOffset += width

            if (node.causesSpace) {
                currentXOffset += font.characterWidth
            }
        }

    }

    /**
     * Draws the textual nodes for the scene.
     */
    private fun drawWords() {
        val nodes = state.currentPageText()
        for ((index, node) in nodes.withIndex()) {
            if (node.padding > 0) {
                currentXOffset += node.font.characterWidth * node.padding
            }

            // skip nodes that are yet to be visible.
            val visible = node.startFrame < state.timer
            if (visible) {
                // reset nodes force a reset of the timer to 0
                if (node.effects.contains(RESET)) {
                    state.timer = 0
                    break
                }

                // glitchy text scroll; if this node would be truncated we use the text of the node
                // one over.
                val nextNode = nodes.getOrNull(index + 1)
                try {
                    if (
                        nextNode != null &&
                        node.startFrame < state.timer &&
                        node.endFrame > state.timer
                    ) {
                        renderTextualNode(node, nextNode)
                    } else {
                        renderTextualNode(node)
                    }
                } catch (e: Exception) {
                    throw SS76EngineInternalError(
                        "Error rendering node:\n${nextNode}",
                        e
                    )
                }
            }

            advanceByNode(node)
        }
    }

    /**
     * Draws the clickable buttons at the top of the scene.
     */
    private fun drawClickables() {
        val font = state.engineState.fontManager.defaultFont
        val drawCheckpoints = state.engineState.settings.enableCheckpoints

        // scroll to the right side again
        val totalWidth = if (drawCheckpoints) {
            font.widthOf("Up / Checkpoint")
        } else {
            font.widthOf("Up")
        }

        var xPos = (viewport.worldWidth - BORDER_PADDING - INNER_PADDING - totalWidth)
        val yPos = (viewport.worldHeight - BORDER_PADDING - INNER_PADDING + (font.characterHeight * 2f))

        run {
            val usable = state.engineState.sceneManager.stackSize > 1
            val colour = if (!usable) Color.SLATE else Color.GREEN

            val rect = renderWord(
                xPos, yPos, font, colour, "Up",
                setOf(), getButtonRect = true,
            )

            if (usable) {
                buttons.addClickableArea(VnButtonManager.GLOBAL_BACK_BUTTON, rect!!)
            }
        }

        if (drawCheckpoints) {
            xPos += font.widthOf("Up")
            renderWord(xPos, yPos, font, Color.WHITE, " / ", setOf())

            xPos += font.widthOf(" / ")

            run {
                val rect = renderWord(
                    xPos, yPos, font, Color.GREEN, "Checkpoint",
                    setOf(), getButtonRect = true,
                )
                buttons.addClickableArea(VnButtonManager.CHECKPOINT_BUTTON, rect!!)
            }
        }
    }

    public fun render() {
        val es = state.engineState
        timedRngRaw.seed(es.globalTimer.floorDiv(30).toInt())
        buttons.reset()

        val definition = state.definition

        // Step 0) Update offsets.
        currentXOffset = 0f
        currentYOffset = 0f

        // Render the background.
        when {
            definition.modifiers.backgroundColour != null -> {
                val colour = definition.modifiers.backgroundColour!!
                clearScreen(colour.r, colour.b, colour.g, colour.a)
            }

            background != null -> {
                clearScreen(0f, 0f, 0f, 1f)
                batch.use(camera) {
                    it.draw(background, 0f, 0f, 1280f, 960f)
                }
            }

            else -> {
                val timer = max(0, state.engineState.globalTimer)
                val blue = 0.25f * sin(timer / 100f) + 0.75f
                val green = 1 - (0.25f * sin(timer / 100f) + 0.75f)
                val red = 1 - (0.25f * cos(timer / 100f) + 0.75f)

                clearScreen(red, green, blue, 0f)
            }
        }

        // Render the text box, if needed. Skipped for text-only mode, where it wouldn't be visible.
        if (!definition.modifiers.textOnlyMode) {
            Gdx.gl.glEnable(GL30.GL_BLEND)
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.use(ShapeRenderer.ShapeType.Filled, camera) {
                it.rect(
                    /* x = */ BORDER_PADDING, /* y = */ BORDER_PADDING,
                    /* width = */ viewport.worldWidth - (75f * 2),
                    /* height = */ viewport.worldHeight - (75f * 2),
                    /* col1 = */ BOX_COLOUR,
                    /* col2 = */ BOX_COLOUR,
                    /* col3 = */ BOX_COLOUR,
                    /* col4 = */ BOX_COLOUR
                )
            }

            Gdx.gl.glDisable(GL30.GL_BLEND)
        }

        batch.use(camera) {
            // draw page buttons
            if (definition.pageCount > 1 && definition.modifiers.drawPageButtons) {
                for (node in getPageButtons()) {
                    renderTextualNode(node)
                    advanceByNode(node)
                }
            }

            // Don't draw words if we're currently fading in.
            if (state.engineState.screenManager.fadeInState != FadeInScreen.FadeInState.FADING_IN) {
                drawWords()
            }

            // draw top-text and clickables
            if (!definition.modifiers.textOnlyMode) {
                drawClickables()

                val topText = definition.modifiers.topText ?: state.engineState.settings.defaultTopText
                val font = state.engineState.fontManager.getFont("top-text")
                val topTextWidth = font.widthOf(topText)

                val yOffset = viewport.worldHeight - 10f

                font.draw(
                    batch,
                    topText,
                    (viewport.worldWidth / 2) - (topTextWidth / 2),
                    yOffset
                )
            }

            if (state.engineState.isDebugMode) {
                state.engineState.fontManager.defaultFont.draw(
                    batch,
                    "Scene ID: ${es.sceneManager.currentScene.definition.sceneId}",
                    15f,
                    50f
                )
            }
        }
    }
}
