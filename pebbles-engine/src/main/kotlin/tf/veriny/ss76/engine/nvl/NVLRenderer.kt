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
import tf.veriny.ss76.engine.VnButtonManager
import tf.veriny.ss76.engine.font.Font
import tf.veriny.ss76.engine.renderer.TextRendererMixin
import tf.veriny.ss76.engine.scene.SceneState
import tf.veriny.ss76.engine.scene.TextualNode
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * A renderer for a scene in NVL mode.
 */
public class NVLRenderer(
    private val state: SceneState,
    private val viewport: Viewport,
    private val camera: OrthographicCamera,
    buttons: VnButtonManager,
) : TextRendererMixin(state.engineState, buttons), Disposable {
    private companion object {
        private val BACKGROUND_BG = Color(48 / 255f, 48 / 255f, 48 / 255f, 0f)

        private val BOX_COLOUR = Color.BLACK.cpy().also { it.a = 0.90f }
    }

    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()


    // lightning effect timer
    private var lastLightningMax = 0
    private var lightningTimer = 0

    init {
        batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined
    }

    override fun dispose() {
        batch.disposeSafely()
        shapeRenderer.disposeSafely()
    }

    /**
     * Raw word renderer. Doesn't handle anything but writing the words to the screen.
     */
    override fun renderWordRaw(
        font: Font,
        colour: Color?,
        word: String,
        effects: Set<TextualNode.Effect>,
        calcRectangle: Boolean
    ): Rectangle? {
        //println(SS76.fontManager.currentFont.fonts.entries)
        val modifiers = state.definition.modifiers
        val bitmapFont = when {
            colour != null -> font.forColour(colour)
            modifiers.textOnlyMode -> font.forColour(modifiers.textOnlyModeColour)
            modifiers.defaultTextColour != null -> font.forColour(modifiers.defaultTextColour)
            else -> font.default
        }

        glyphLayout.setText(bitmapFont, word)

        var xOffset = padding + currentXOffset
        var yOffset = viewport.worldHeight - padding - currentYOffset

        if (TextualNode.Effect.SHAKE in effects) {
            xOffset += Random.Default.nextInt(-2, 2)
            yOffset += Random.Default.nextInt(-1, 1)
        }

        bitmapFont.draw(batch, word, xOffset, yOffset)
        // no space, that's handled by the external code
        // avoid calculating rectangles for anything that isn't a link node.
        val rect = if (calcRectangle) {
            val extraWidth = font.characterWidth
            Rectangle(
                padding + currentXOffset - (extraWidth / 2),
                (viewport.worldHeight - padding) - currentYOffset - glyphLayout.height,
                glyphLayout.width + extraWidth, glyphLayout.height
            )
        } else null

        currentXOffset += glyphLayout.width

        return rect
    }

    // cache page buttons

    /**
     * Renders the paging buttons.
     */
    private fun getPageButtons(state: SceneState): List<TextualNode> {
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

        return nodes
    }

    private fun drawClickables(border: Float) {
        val font = es.fontManager.defaultFont
        val width = font.characterWidth

        currentXOffset = -width
        currentYOffset = -glyphLayout.height * 2

        // scroll to the right side again
        glyphLayout.setText(font.default, "Up / Checkpoint")
        currentXOffset = (viewport.worldWidth - padding - border - (glyphLayout.width))

        run {
            val rect = renderWordRaw(font, Color.GREEN, "Up", calcRectangle = true)
            buttons.addClickableArea(VnButtonManager.GLOBAL_BACK_BUTTON, rect!!)
        }
        currentXOffset += width
        renderWordRaw(font, Color.WHITE, "/")
        currentXOffset += width
        run {
            val rect = renderWordRaw(font, Color.GREEN, "Checkpoint", calcRectangle = true)
            buttons.addClickableArea(VnButtonManager.CHECKPOINT_BUTTON, rect!!)
        }
    }

    /**
     * Called when the scene is rendered.
     */
    public fun render() {
        // make sure the random effects are the same every 30f
        //rng.setSeed(state.engineState.globalTimer.floorDiv(30))
        buttons.reset()

        val definition = state.definition

        // Step 0) Update offsets.
        currentXOffset = 0f
        currentYOffset = 0f

        val border = 75f

        // step 1) Draw the background.
        when {
            definition.modifiers.backgroundName != null -> {
                val bg = state.engineState.assets.getBackground(definition.modifiers.backgroundName!!)
                clearScreen(0f, 0f, 0f, 1f)
                batch.use(camera) {
                    it.draw(bg, 0f, 0f, 1280f, 960f)
                }
            }

            definition.modifiers.backgroundColour != null -> {
                val colour = definition.modifiers.backgroundColour!!
                clearScreen(colour.r, colour.b, colour.g, colour.a)
            }

            else -> {
                val timer = max(0, es.globalTimer)
                val blue = 0.25f * sin(timer / 100f) + 0.75f
                val green = 1 - (0.25f * sin(timer / 100f) + 0.75f)
                val red = 1 - (0.25f * cos(timer / 100f) + 0.75f)

                clearScreen(red, green, blue, 0f)
            }
        }

        // Step 2) Render the black box.
        if (!definition.modifiers.textOnlyMode) {
            Gdx.gl.glEnable(GL30.GL_BLEND)
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.use(ShapeRenderer.ShapeType.Filled, camera) {
                it.rect(
                    75f, 75f, 1280 - (75f * 2), 960 - (75f * 2),
                    BOX_COLOUR, BOX_COLOUR, BOX_COLOUR, BOX_COLOUR
                )
            }

            Gdx.gl.glDisable(GL30.GL_BLEND)
        }

        // Step 3) Begin drawing.
        batch.use(camera) {
            // 3a) Draw pages if needed.
            if (definition.pageCount > 1 && definition.modifiers.drawPageButtons) {
                val pageButtons = getPageButtons(state)
                for (node in pageButtons) {
                    renderTextNode(state, node)
                }
            }

            // 3b) Draw the current nodes, including glitchy nodes.
            drawWords(state)

            if (!definition.modifiers.textOnlyMode) {
                // 4) Draw clickables.
                drawClickables(border)

                // 5) Draw top text.
                val topText = /*definition.effects.topText ?:*/ "MAGELLANIC GAP"
                glyphLayout.setText(es.fontManager.topTextFont.default, topText)
                val yOffset = viewport.worldHeight - 10f
                es.fontManager.topTextFont.default.draw(
                    batch,
                    topText,
                    (viewport.worldWidth / 2) - (glyphLayout.width / 2),
                    yOffset
                )
            }


            // 6) Draw debug scene data
            if (es.isDebugMode) {
                es.fontManager.defaultFont.default.draw(
                    batch,
                    "Scene ID: ${es.sceneManager.currentScene.definition.sceneId}",
                    15f,
                    50f
                )
            }
        }
    }

}
