package tf.veriny.ss76.engine.psm

/**
 * Thrown for syntax errors during PSM parsing.
 */
public open class PsmSyntaxError(message: String, cause: Throwable? = null) : Exception(message, cause)

public class PsmEndOfSceneError : PsmSyntaxError("reached end of scene!")