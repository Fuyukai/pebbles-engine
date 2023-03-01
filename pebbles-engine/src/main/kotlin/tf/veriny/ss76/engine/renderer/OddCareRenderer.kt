/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ktx.freetype.generateFont
import tf.veriny.ss76.use

/**
 * Demo button at the top of the screen.
 */
internal class OddCareRenderer {
    private val demoFont: BitmapFont
    private val batch = SpriteBatch()

    init {
        val font = FreeTypeFontGenerator(Gdx.files.internal("fonts/Mx437_Wang_Pro_Mono.ttf"))

        demoFont = font.generateFont {
            size = 42
            color = RED
            mono = true
        }
    }

    // one second off, 2 seconds on
    private var timer = 0

    internal fun render() {
        if (timer.rem(90) > 30) {
            batch.use {
                demoFont.draw(batch, "DEMO", 10f, Gdx.graphics.height - 10f)
            }
        }

        timer++

    }
}