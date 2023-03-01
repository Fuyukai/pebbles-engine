/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76

import com.badlogic.gdx.Gdx
import ktx.app.KtxApplicationAdapter
import tf.veriny.ss76.engine.*
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.util.EktFiles
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Main game object.
 */
@OptIn(ExperimentalTime::class)
@Suppress("GDXKotlinStaticResource")  // don't care, these will never be disposed
public class SS76(private val callback: (EngineState) -> Unit) : KtxApplicationAdapter {
    public companion object {
        public val IS_DEMO: Boolean =
            System.getProperty("demo", "false").toBooleanStrict()
    }

    private lateinit var state: EngineState
    private var errorScreen: ErrorScreen? = null
    private var renderTime = 0L

    override fun create() {
        Gdx.files = EktFiles

        try {
            createImpl()
        } catch (e: Exception) {
            val error = ErrorScreen(null, e)
            errorScreen = error
        }
    }

    private fun createImpl() {
        state = EngineState()
        state.created(callback)

        Gdx.input.inputProcessor = state.input
    }

    private fun debugRender() {
        val time = measureTime { state.render() }.inWholeNanoseconds
        renderTime += time
    }

    override fun render() {
        val err = errorScreen
        if (err != null) {
            return err.render(Gdx.graphics.deltaTime)
        }

        try {
            if (state.isDebugMode) {
                debugRender()
            } else {
                state.render()
            }
        } catch (e: Exception) {
            state.screenManager.changeScreen(ErrorScreen(state, e))
        }
    }


}
