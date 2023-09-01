package tf.veriny.ss76.engine.psm

import tf.veriny.ss76.engine.scene.builder.SceneBuilder.PageBuilder

/**
 * Inserts the provided number of newlines.
 */
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

/**
 * Adds a new raw line with the provided amount of newlines after it.
 */
public fun PageBuilder.nlline(content: String, newlines: Int = 1) {
    rline(content)
    nl(newlines)
}

/**
 * Adds a new change scene button with the provided content.
 */
public fun PageBuilder.cs(scene: String, content: String) {
    addRawFragment(" $[`=cs-${scene},@=salmon] $content $$ ")
}

/**
 * Adds a new push scene button with the provided content.
 */
public fun PageBuilder.ps(scene: String, content: String, colour: String? = null) {
    if (colour == null) {
        addRawFragment(" $[`=ps-${scene},%=true] $content $$ ")
    } else {
        addRawFragment(" $[`=ps-${scene},@=$colour] $content $$ ")
    }
}

/**
 * Adds a new dialogue-line using the dialogue macro.
 */
public fun PageBuilder.dline(name: String, content: String) {
    addRawFragment(" %dl(SOMEBODY)% $content $$ ")
}