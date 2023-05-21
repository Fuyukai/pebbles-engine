/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene.builder

import tf.veriny.ss76.engine.psm.UnbakedScene
import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.SceneModifiers
import tf.veriny.ss76.engine.scene.register

/**
 * A sequence of scenes that all share the same modifiers.
 */
public class SceneSequence
internal constructor(public val sm: SceneManager) : HasModifiers {
    override var modifiers: SceneModifiers = SceneModifiers()

    /**
     * Registers a scene with the shared modifiers this sequence is currently using.
     */
    public inline fun register(
        sceneId: String,
        crossinline block: SceneBuilder.() -> Unit
    ): UnbakedScene {
        return sm.register(sceneId) {
            modifiers = this@SceneSequence.modifiers
            block()
        }
    }

    /**
     * Registers a single-fragment scene with the shared modifiers this sequence is currently using.
     */
    public fun register(sceneId: String): UnbakedScene {
        return register(sceneId) {
            page(sceneId)
        }
    }
}