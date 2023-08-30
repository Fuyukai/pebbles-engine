package tf.veriny.ss76.engine.psm

import tf.veriny.ss76.engine.scene.builder.SceneBuilder
import tf.veriny.ss76.engine.scene.builder.SceneBuilder.PageBuilder

public fun PageBuilder.nl(count: Int = 1) {
    when {
        count <= 0 -> {}
        count == 1 -> { addRawFragment(PsmFragmentManager.RAW_SINGLE_NEWLINE) }
        else -> { addRawFragment(" $[nl=${count}] ") }
    }
}

/**
 * Creates a new raw fragment with the provided literal line content.
 */
public fun PageBuilder.rline(content: String) {
    addRawFragment(content)
}

public fun PageBuilder.nlline(content: String, newlines: Int = 1) {
    rline(content)
    nl(newlines)
}