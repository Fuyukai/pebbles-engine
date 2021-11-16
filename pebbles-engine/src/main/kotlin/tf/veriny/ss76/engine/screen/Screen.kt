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

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.utils.Disposable
import ktx.app.KtxInputAdapter

/**
 * A screen is responsible for managing sub-renderers.
 */
public interface Screen : KtxInputAdapter, Disposable {
    /**
     * Renders the current screen.
     */
    public fun render(delta: Float)
}