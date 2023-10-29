package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.InputProcessor
import tf.veriny.ss76.EngineState

/**
 * A shim screen that contains references to both the previous and current screen.
 */
public class ShimScreen
internal constructor(
    public val previousScreen: Screen,
    public val currentScreen: Screen,
) : Screen {
    override fun dispose() {
        currentScreen.dispose()
    }

    override fun render(delta: Float) {
        currentScreen.render(delta)
    }

    override fun getInputProcessors(): Collection<InputProcessor> {
        return currentScreen.getInputProcessors()
    }

    override fun deactivated() {
        return currentScreen.deactivated()
    }
}
