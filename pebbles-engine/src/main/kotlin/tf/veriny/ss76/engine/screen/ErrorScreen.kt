/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ktx.app.clearScreen
import ktx.freetype.generateFont
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.util.EktFiles
import tf.veriny.ss76.engine.util.NioFileHandle
import tf.veriny.ss76.use
import kotlin.io.path.toPath

/**
 * The screen for rendering errors.
 */
public class ErrorScreen(
    private val state: EngineState?,
    public val error: Throwable,
) : Screen {
    private var hasPrinted = false

    private val batch = SpriteBatch()
    private val emergencyFont: BitmapFont? = run {
        val path = EktFiles.RESOLVER.getPath("engine/fonts/Mx437_Wang_Pro_Mono.ttf")
                   ?: return@run null

        val generator = FreeTypeFontGenerator(
            NioFileHandle(path, Files.FileType.Classpath)
        )
        generator.generateFont { size = 24; color = Color.WHITE; mono = true }
    }

    private val live = run {
        val path = EktFiles.RESOLVER.getPath("gfx/live.png")
                   ?: return@run null
        Texture(NioFileHandle(path, Files.FileType.Classpath))
    }

    init {
        state?.musicManager?.stop()
    }

    override fun render(delta: Float) {
        clearScreen(255f, 0f, 0f, 0f)

        val tb = error.stackTraceToString()
        if (!hasPrinted) {
            error.printStackTrace()
            hasPrinted = true
        }

        batch.use {
            if (state == null) {
                if (emergencyFont == null) {
                    if (live == null) {
                        return
                    }

                    val x = (Gdx.graphics.width / 2f) - live.width / 2f
                    val y = (Gdx.graphics.height / 2f) - live.height / 2f
                    draw(live, x, y)
                } else {
                    emergencyFont.draw(
                        this,
                        "Fatal error when loading engine!",
                        1f,
                        Gdx.graphics.height - 10f
                    )

                    emergencyFont.draw(this, tb, 1f, Gdx.graphics.height - 30f)
                }

                return
            }

            if (state.isDebugMode) {
                val message = if (state.sceneManager.stackSize == 0) {
                    "Fatal error!"
                } else {
                    "Fatal error when rendering scene ${state.sceneManager.currentScene.definition.sceneId}"
                }

                state.fontManager.errorFont.draw(
                    this, message,
                    1f,
                    Gdx.graphics.height - 10f
                )

                state.fontManager.errorFont.draw(this, tb, 1f, Gdx.graphics.height - 30f)
            } else {
                if (live != null) {
                    val x = (Gdx.graphics.width / 2f) - live.width / 2f
                    val y = (Gdx.graphics.height / 2f) - live.height / 2f
                    draw(live, x, y)
                }
            }

            Unit
        }
    }

    override fun dispose() {

    }
}
