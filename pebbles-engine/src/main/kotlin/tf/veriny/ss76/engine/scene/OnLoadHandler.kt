package tf.veriny.ss76.engine.scene

/**
 * Helper interface for when a scene is loaded.
 */
public fun interface OnLoadHandler {
    /**
     * Called immediately before a scene is loaded. Can be used to do things like play music,
     * swap pages, etc.
     */
    public fun onLoad(state: SceneState)
}