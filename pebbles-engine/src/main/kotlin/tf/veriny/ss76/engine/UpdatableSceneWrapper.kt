/*
 * This file is part of Pebbles.
 *
 * Pebbles is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pebbles is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pebbles.  If not, see <https://www.gnu.org/licenses/>.
 */

package tf.veriny.ss76.engine

import tf.veriny.ss76.engine.scene.*

/**
 * A wrapper that re-registers a scene every time it updates. This allows for simple dynamic scenes
 * (e.g. the checkpoint menu or the the Record).
 *
 * A dynamic scene is saveable. A dynamic scene cannot use onLoad handlers due to this.
 */
public open class UpdatableSceneWrapper(
    public val sceneId: String
) {
    private val buttons = mutableMapOf<String, Button>()

    // starts with one page of stringbuilder, for the first page
    private var pages = mutableListOf<StringBuilder>(StringBuilder())

    public fun register(sceneManager: SceneManager) {
        println("re-registering $sceneId")
        val parsedPages = pages.map { splitScene(it.toString()) }

        val definition = SceneDefinition(
            sceneId, buttons, parsedPages, this.pages.map { it.toString() },
            effects = SceneEffects.NONE,
            dynamic = true,
        )
        sceneManager.reregisterScene(definition)
    }

    /**
     * Resets the scene. A subsequent call to register() is required.
     */
    public fun reset() {
        pages = mutableListOf(StringBuilder())
        buttons.clear()
    }

    /**
     * Adds a new button.
     */
    public fun addButton(button: Button) {
        buttons[button.name] = button
    }

    /**
     * Updates the number of pages in this scene.
     */
    public fun updatePageLength(length: Int) {
        when {
            pages.size == length -> return
            pages.size > length -> {
                pages = pages.dropLast((pages.size - length)).toMutableList()
            }
            pages.size > length -> {
                (0 until (pages.size - length)).forEach { _ -> pages.add(StringBuilder()) }
            }
        }
    }

    // not so good API, but it works.
    /**
     * Clears the page specified.
     */
    public fun clearPage(page: Int) {
        pages[page].clear()
    }

    /**
     * Edits the specified page by index.
     */
    public open fun edit(page: Int, block: PageBuilder.() -> Unit) {
        val sb = pages[page]
        val builder = PageBuilder(sb) { buttons[it.name] = it }
        builder.block()
    }
}