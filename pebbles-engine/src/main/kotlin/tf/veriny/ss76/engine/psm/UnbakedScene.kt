package tf.veriny.ss76.engine.psm

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.ChangeSceneButton
import tf.veriny.ss76.engine.PushSceneButton
import tf.veriny.ss76.engine.scene.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val PUSH_REGEX = "^(?:push-scene-|ps-)(.*)".toRegex()
private val CHANGE_REGEX = "^(?:change-scene-|cs-)(.*)".toRegex()

private val buttonProviders = listOf<Pair<Regex, (String, String) -> Button>>(
    PUSH_REGEX to { id, scene -> PushSceneButton(id, scene) },
    CHANGE_REGEX to { id, scene -> ChangeSceneButton(id, scene) }
)

/**
 * A scene that contains multiple pages that have not yet been parsed.
 */
public class UnbakedScene(
    public val sceneId: String,
    private val pages: List<List<PsmIncludedFragment>>,
    private val buttons: List<Button>,
    private val modifiers: SceneModifiers,
    private val onLoadHandlers: List<OnLoadHandler>
) {
    private var cachedScene: VirtualNovelSceneDefinition? = null

    private fun bakePage(
        page: List<PsmIncludedFragment>,
        state: EngineState,
    ): List<TextualNode> {
        val built = StringBuilder()
        for (fragment in page) {
            if (!fragment.shouldBeIncluded(state)) continue

            built.append(
                if (fragment.isInstant) {
                    fragment.asInstant()
                } else {
                    fragment.fragment.content
                }
            )
        }

        return state.sceneManager.sceneBakery.bake(built.toString())
    }

    /**
     * Bakes this scene into a [VirtualNovelSceneDefinition], ready to be displayed.
     */
    @OptIn(ExperimentalTime::class)
    public fun bake(
        state: EngineState,
        isPreBaking: Boolean = false,
        force: Boolean = false
    ): VirtualNovelSceneDefinition {
        val isStatic = pages.all { it.all { inner -> inner.isStatic() } }
        if (!force && isStatic && cachedScene != null) {
            return cachedScene!!
        }

        println("SCENE BAKERY: ${if (!isPreBaking) "JIT " else ""}baking scene $sceneId")
        val bakeTime = measureTimedValue {
            pages.map { bakePage(it, state) }
        }
        val us = bakeTime.duration.inWholeMicroseconds
        println("SCENE BAKERY: Took ${us/1000}ms!")
        if (!isPreBaking && us > 1_000_000/60) {
            println("Warning: Scene baking took too long! Consider asking for it to be pre-baked!")
        }


        val buttonMap = buttons.associateByTo(mutableMapOf()) { it.name }

        // search through the nodes and find any auto-buttons
        for (node in bakeTime.value.flatten()) {
            if (node.buttonId.isNullOrBlank()) continue
            val buttonId = node.buttonId!!
            if (buttonId in buttonMap) continue

            for ((regexp, fn) in buttonProviders) {
                val match = regexp.matchEntire(buttonId)
                if (match != null) {
                    val value = match.groupValues[1]
                    val button = fn(buttonId, value)
                    buttonMap[buttonId] = button
                    break
                }
            }
        }

        val scene = SceneDefinition(
            sceneId = sceneId,
            originalButtons = buttonMap,
            pages = bakeTime.value,
            originalPages = emptyList(),  // todo: do we care?
            onLoadHandlers = onLoadHandlers,
            modifiers = modifiers,
        )

        if (isStatic) cachedScene = scene
        return scene
    }
}