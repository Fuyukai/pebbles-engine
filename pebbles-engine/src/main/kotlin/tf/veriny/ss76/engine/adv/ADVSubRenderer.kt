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

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import ktx.app.KtxInputAdapter
import tf.veriny.ss76.engine.scene.SceneState

/**
 * The ADB sub-renderer allows drawing things above the ADV dialogue box.
 *
 * The renderer uses a camera (1280x960), so you can safely draw on it without worrying about
 * screen resolutions.
 */
public interface ADVSubRenderer : Disposable, KtxInputAdapter {
    /**
     * Creates any resources required for this sub-renderer.
     */
    public fun create()

    /**
     * Renders this sub-renderer.
     */
    public fun render(batch: SpriteBatch, camera: OrthographicCamera, sceneState: SceneState)
}