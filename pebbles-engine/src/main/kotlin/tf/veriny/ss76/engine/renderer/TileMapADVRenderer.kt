/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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