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