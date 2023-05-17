package tf.veriny.ss76.engine.scene

import tf.veriny.ss76.engine.psm.UnbakedScene
import tf.veriny.ss76.engine.scene.builder.SceneBuilder

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
public fun SceneManager.register(sceneId: String): UnbakedScene {
    val builder = SceneBuilder(state, sceneId)
    builder.page {
        addFragment(sceneId)
    }
    return builder.get().also { registerScene(it) }
}