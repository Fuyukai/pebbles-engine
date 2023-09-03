/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
