package tf.veriny.pebbles

import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.createAndRegisterScene
import tf.veriny.ss76.engine.scene.sceneSequence
import tf.veriny.ss76.engine.util.NAMED_COLOURS

public fun SceneManager.registerDemoScenes(): Unit = sceneSequence{
    defaultModifiers = defaultModifiers.copy(
        alwaysAllowTextSkip = true,
        drawPageButtons = true,
    )

    createAndRegisterScene("demo-meta-menu") {
        page {
            line("This is the Pebbles Engine demo menu.")
            newline()

            line("Consult the source code for the implementations of these effects.")
            newline()

            changeSceneButton("demo.colours", "Colour demonstration")
        }
    }

    createAndRegisterScene("demo.colours") {
        page {
            line("Named colours:")
            newline()

            for (name in NAMED_COLOURS.keys) {
                line("Colour: @${name}@${name}")
            }
        }

        page {
            line("New in 0.8: Rainbow effects!")
            newline()

            line(":push:¬rainbowify¬ This text is rainbowified! :pop:")
        }
    }
}