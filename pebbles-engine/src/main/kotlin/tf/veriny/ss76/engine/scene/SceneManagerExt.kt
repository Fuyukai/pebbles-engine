/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.engine.psm.UnbakedScene
import tf.veriny.ss76.engine.scene.builder.SceneBuilder
import tf.veriny.ss76.engine.scene.builder.SceneSequence

/**
 * Creates and registers a new scene with the specified scene ID.
 */
public fun SceneManager.register(sceneId: String, block: SceneBuilder.() -> Unit): UnbakedScene {
    val builder = SceneBuilder(state, sceneId)
    builder.block()
    return builder.get().also { registerScene(it) }
}

/**
 * Creates and registers a single-fragment scene.
 */
public fun SceneManager.register(
    sceneId: String
): UnbakedScene {
    val builder = SceneBuilder(state, sceneId)
    builder.page {
        addFragment(sceneId)
    }
    return builder.get().also { registerScene(it) }
}

/**
 * Creates a new [SceneSequence].
 */
public fun SceneManager.sceneSequence(block: SceneSequence.() -> Unit){
    SceneSequence(this).also(block)
}