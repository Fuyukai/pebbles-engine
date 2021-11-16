/*
 * This file is part of Pebbles.
 *
 * Pebbles is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pebbles is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pebbles.  If not, see <https://www.gnu.org/licenses/>.
 */

package tf.veriny.ss76.engine.adv

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.FontManager
import tf.veriny.ss76.engine.renderer.TextRendererMixin
import tf.veriny.ss76.engine.scene.SceneState
import tf.veriny.ss76.engine.scene.TextualNode
import tf.veriny.ss76.roundedRect
import tf.veriny.ss76.use
import kotlin.random.Random

/**
 * The renderer for ADV scenes.
 */
public class ADVRenderer(
    public val subRenderer: ADVSubRenderer,
    es: EngineState,
) : TextRendererMixin(es) {
    private val srBatch = SpriteBatch()
    private val textBatch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()

    private val camera = OrthographicCamera(1280f, 960f).also {
        it.setToOrtho(false, 1280f, 960f)
        it.update()

        srBatch.projectionMatrix = it.combined
    }

    override val padding: Float
        get() = 90f

    private val yBoxOffset get() = 3 * (Gdx.graphics.height.toFloat() / 4f)

    override fun renderWordRaw(
        font: FontManager.Font,
        colour: Color,
        word: String,
        effects: Set<TextualNode.Effect>,
        calcRectangle: Boolean
    ): Rectangle? {
        val bitmapFont = font.fonts[colour]!!
        glyphLayout.setText(bitmapFont, word)

        var xOffset = padding + currentXOffset
        var yOffset = (Gdx.graphics.height - yBoxOffset) - currentYOffset

        if (TextualNode.Effect.SHAKE in effects) {
            xOffset += Random.Default.nextInt(-2, 2)
            yOffset += Random.Default.nextInt(-1, 1)
        }

        bitmapFont.draw(textBatch, word, xOffset, yOffset)

        // no space, that's handled by the external code
        // avoid calculating rectangles for anything that isn't a link node.
        val rect = if (calcRectangle) {
            val extraWidth = font.characterWidth
            Rectangle(
                padding + currentXOffset - (extraWidth / 2),
                (Gdx.graphics.height - yBoxOffset) - currentYOffset - glyphLayout.height,
                glyphLayout.width + extraWidth, glyphLayout.height
            )
        } else null

        currentXOffset += glyphLayout.width

        return rect
    }

    /**
     * Renders the ADV mode screen.
     */
    public fun render(sceneState: SceneState) {
        reseedRng(sceneState.timer)
        es.buttonManager.reset()
        srBatch.use { subRenderer.render(srBatch, camera, sceneState) }

        //shape.projectionMatrix = camera.combined
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            color = Color.BLACK
            val pad = 75f
            roundedRect(pad, 15f,
                Gdx.graphics.width - (pad * 2),
                Gdx.graphics.height - yBoxOffset,
                15f
            )
        }

        textBatch.use {
            currentXOffset = 0f
            currentYOffset = 0f
            drawWords(sceneState)
        }



        sceneState.timer++
    }
}