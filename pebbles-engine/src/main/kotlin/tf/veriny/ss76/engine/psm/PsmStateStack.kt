package tf.veriny.ss76.engine.psm

import com.badlogic.gdx.graphics.Color
import tf.veriny.ss76.engine.scene.TextualNode

// internal usage: see scenebaker
// basically whenever it sees a directive e.g. ``$[@=green]`` it pushes a StackEntry with the
// specified properties
// then each property walks the stack in reverse and gets that value until its not null.
// this is a pretty ugly class but it gets the job done well.
// another potential alternative is a sealed interface hierachy... investigate that later.
// getTopProperty<Type>()

/**
 * A stack of the current state during scene baking.
 */
internal class PsmStateStack(private val default: StackEntry) {
    internal class StackEntry(
        /** The colour to render the node with. */
        var colour: Color? = null,
        /** The number of linger frames to add to nodes. */
        var linger: Int? = null,  // eww, boxed integers
        /** If there should be no extra spaces added after words. */
        var chomp: Boolean? = null,
        /** The per-node effects to use. */
        var effects: MutableSet<TextualNode.Effect> = mutableSetOf(),

        /** The font that the node uses. */
        var font: String? = null,

        /** If the colour of the node is linked to the button's seen status. */
        var colourLinkedToButton: Boolean? = null,

        /** The ID of the button that the textual nodes will use. */
        var lastButton: String? = null,

        /** If extra frames are added at double-newlines. */
        var enableNewlineLinger: Boolean? = null,
        /** The frames to add for newline linger, if any. */
        var newlineLingerFrames: Int? = null,

        /** If true, then any additions to the frame counter will be ignored. */
        var instant: Boolean? = null,

        /** The number of frames it should take for a single word to render. */
        var framesPerWord: Int? = null,

        /** The right margin, used for word wrapping. */
        var rightMargin: Int? = null,

        /** The left margin, used for automatically indenting. */
        var leftMargin: Int? = null,

        val temp: Boolean = false,
    )

    private val stack = ArrayDeque<StackEntry>()

    inline fun <T> getValueOrNull(block: (StackEntry) -> T?): T? {
        for (i in stack.asReversed()) {
            val res = block(i)
            if (res != null) return res
        }

        return null
    }

    inline fun <T> getValue(block: (StackEntry) -> T?): T {
        return getValueOrNull(block) ?: error("missing default value!")
    }

    val top: StackEntry get() = stack.last()

    val colour: Color? get() = getValueOrNull { it.colour }
    val linger: Int get() = getValueOrNull { it.linger } ?: 0
    val chomp: Boolean get() = getValueOrNull { it.chomp } ?: false
    val effects: Set<TextualNode.Effect> get() = stack.asReversed().flatMapTo(mutableSetOf()) { it.effects }
    val instant: Boolean get() = getValueOrNull { it.instant } ?: false
    val lastButton: String? get() = getValueOrNull { it.lastButton }
    val colourLinkedToButton: Boolean get() = getValueOrNull { it.colourLinkedToButton } ?: false
    val font: String get() = getValueOrNull { it.font } ?: "default"

    val enableNewlineLinger: Boolean get() = getValueOrNull { it.enableNewlineLinger } ?: true
    val newlineLingerFrames: Int get() = getValueOrNull { it.newlineLingerFrames } ?: 45
    val framesPerWord: Int get() = getValueOrNull { it.framesPerWord } ?: 5
    val rightMargin: Int get() = getValueOrNull { it.rightMargin } ?: 70
    val leftMargin: Int get() = getValueOrNull { it.leftMargin } ?: 0


    fun push(entry: StackEntry) = stack.addLast(entry)
    fun pop() = stack.removeLast()

    init {
        clear()
    }

    fun clear() {
        stack.clear()
        stack.add(default)
    }

    fun removeTemp() {
        if (stack.last().temp) stack.removeLast()
    }
}