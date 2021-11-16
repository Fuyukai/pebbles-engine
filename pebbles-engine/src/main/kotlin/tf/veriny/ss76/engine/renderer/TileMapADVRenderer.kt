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

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer
import tf.veriny.ss76.engine.adv.ADVSubRenderer
import tf.veriny.ss76.engine.scene.SceneState

/**
 * Base class for rendering a tiled map.
 */
public abstract class TileMapADVRenderer(private val mapFile: String) : ADVSubRenderer {
    private companion object {
        private val loader = TmxMapLoader(InternalFileHandleResolver())
    }

    protected lateinit var map: TiledMap
    protected lateinit var renderer: OrthoCachedTiledMapRenderer

    override fun create() {
        map = loader.load(mapFile)
        renderer = OrthoCachedTiledMapRenderer(map)
    }

    override fun dispose() {
        map.dispose()
        renderer.dispose()
    }

    override fun render(batch: SpriteBatch, camera: OrthographicCamera, sceneState: SceneState) {
        renderer.setView(camera)
        renderer.render()

        renderOverMap(batch, camera, sceneState)
    }

    /**
     * Renders something over the map.
     */
    protected abstract fun renderOverMap(
        batch: SpriteBatch, camera: OrthographicCamera, sceneState: SceneState
    )

}