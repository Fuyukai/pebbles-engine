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

package tf.veriny.ss76.engine.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ktx.freetype.generateFont
import tf.veriny.ss76.SS76
import tf.veriny.ss76.use

/**
 * Demo button at the top of the screen.
 */
internal class OddCareRenderer {
    private val demoFont: BitmapFont
    private val batch = SpriteBatch()

    init {
        val font = FreeTypeFontGenerator(Gdx.files.internal("fonts/MxPlus_Cordata_PPC-21.ttf"))

        demoFont = font.generateFont {
            size = if (!SS76.isBabyScreen) {
                42
            } else {
                21
            }
            color = RED
            mono = true
        }
    }

    // one second off, 2 seconds on
    private var timer = 0

    internal fun render() {
        if (timer.rem(90) > 30) {
            batch.use {
                demoFont.draw(batch,"DEMO", 10f, Gdx.graphics.height - 10f)
            }
        }

        timer++

    }
}