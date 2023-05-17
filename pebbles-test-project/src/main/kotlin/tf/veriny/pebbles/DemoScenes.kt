package tf.veriny.pebbles

import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.register

public fun SceneManager.registerDemoScenes(): Unit {
    register("demo-meta-menu")
    register("demo.colours")
}