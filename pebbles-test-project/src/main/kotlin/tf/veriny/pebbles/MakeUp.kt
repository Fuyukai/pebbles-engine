package tf.veriny.pebbles

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.SS76Settings
import tf.veriny.ss76.isInsideJar

public object MakeUp {
    @JvmStatic
    public fun main(args: Array<String>) {
        val settings = SS76Settings(
            namespace = "pebbles-test-project",
            initialiser = ::setupEngine,
            isDebugMode = !isInsideJar(MakeUp::class)
        )
        SS76.start(settings)
    }

    public fun setupEngine(state: EngineState) {

    }
}