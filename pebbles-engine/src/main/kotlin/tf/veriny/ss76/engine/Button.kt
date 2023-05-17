/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import tf.veriny.ss76.EngineState

/**
 * A single button.
 */
public interface Button {
    /** The name of this button. */
    public val name: String

    /** The linked scene ID for this button, if any. */
    public val linkedId: String?
        get() = null

    /**
     * Does something when this button is clicked.
     */
    public fun run(state: EngineState)
}

/**
 * A button for changing scene.
 */
public class ChangeSceneButton(
    public override val name: String,
    public override val linkedId: String,
    /** The event flag to set. */
    public val setFlag: String? = null,
    /** The value of the event flag to set. */
    public val eventValue: Int = 0,
) : Button {

    override fun run(state: EngineState) {
        if (setFlag != null) state.eventFlagsManager.set(setFlag, eventValue)
        state.sceneManager.swapScene(linkedId)
    }
}

/**
 * A button for pushing a new scene.
 */
public class PushSceneButton(
    public override val name: String,
    public override val linkedId: String,
    /** The event flag to set. */
    public val setFlag: String? = null,
    /** The value of the event flag to set. */
    public val eventValue: Int = 0,
) : Button {
    override fun run(state: EngineState) {
        if (setFlag != null) state.eventFlagsManager.set(setFlag, eventValue)
        state.sceneManager.pushScene(linkedId)
    }
}

/**
 * A button that exits the current scene.
 */
public object BackButton : Button {
    override val name: String = "back-button"

    override fun run(state: EngineState) {
        state.sceneManager.exitScene()
    }
}

public object PrevPageButton : Button {
    override val name: String = "prev-page"

    override fun run(state: EngineState) {
        state.sceneManager.currentScene.pageBack()
    }
}

public object NextPageButton : Button {
    override val name: String = "next-page"

    override fun run(state: EngineState) {
        state.sceneManager.currentScene.pageNext()
    }
}

