package tf.veriny.pebbles

import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.register
import tf.veriny.ss76.engine.scene.sceneSequence

public fun SceneManager.registerDemoScenes(): Unit = sceneSequence {
    sharedModifiers = sharedModifiers.copy(drawPageButtons = true)

    register("demo-meta-menu")
    register("demo.colours") {
        page("demo.colours.1")
        page("demo.colours.2")
    }
    register("demo.effects")
    register("demo.font-changes")
}