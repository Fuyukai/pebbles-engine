package tf.veriny.ss76.engine.psm

/**
 * A single fragment in a scene.
 *
 * Fragments allow building up a scene progressively, potentially depending on previous choices.
 */
@JvmInline
public value class PsmSceneFragment(public val content: String) {
    public fun asInstant(): PsmSceneFragment {
        return PsmSceneFragment("$[instant=] $content $[pop=]")
    }
}