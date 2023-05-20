/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.app.KtxApplicationAdapter
import tf.veriny.ss76.engine.*
import tf.veriny.ss76.engine.nvl.NVLScreen
import tf.veriny.ss76.engine.renderer.OddCareRenderer
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.system.registerSystemScenes
import kotlin.time.ExperimentalTime

/**
 * Main game object.
 */
@Suppress("GDXKotlinStaticResource", "NAME_SHADOWING")  // don't care, these will never be disposed
public class SS76(
    private val namespace: String,
    private val registrar: SceneRegistrar,
    private val defaultTopText: String,
) : KtxApplicationAdapter {
    public companion object {
        public val IS_DEMO: Boolean =
            System.getProperty("demo", "false").toBooleanStrict()

        public val IS_DEBUG: Boolean =
            System.getProperty("debug", "true").toBooleanStrict()

        /** If this is a smaller screen size. */
        public val isBabyScreen: Boolean by lazy {
            Gdx.graphics.height < 960
        }

        public var ENABLE_INVENTORY: Boolean = false
    }

    private lateinit var state: EngineState

    /** The global monotonic timer. Never decrements. */
    public var globalTimer: Int = 0

    override fun create() {
        try {
            createImpl()
        } catch (e: Exception) {
            state.screenManager.changeScreen(ErrorScreen(state, e))
            return
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun createImpl() {
        state = EngineState(namespace, registrar, defaultTopText)
        state.created()

        Gdx.input.inputProcessor = state.input
    }

    override fun render() {
        state.render()
    }


}