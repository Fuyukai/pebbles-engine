package tf.veriny.ss76.engine.psm

/**
 * A macro in a PSM file that expands out to other text.
 */
public fun interface PsmMacro {
    /**
     * Invokes this macro, returning the expanded text.
     */
    public operator fun invoke(vararg args: String): String
}