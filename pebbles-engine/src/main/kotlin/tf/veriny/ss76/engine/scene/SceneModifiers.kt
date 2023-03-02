/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene

import com.badlogic.gdx.graphics.Color

/**
 * Contains state for the modifiers for a scene.
 */
public data class SceneModifiers(
    /**
     * The default colour for text without an explicit colour.
     * If unset, this will be the font's default colour.
     */
    public val defaultTextColour: Color? = null,
    /** The default colour in text-only mode. */
    public val textOnlyModeColour: Color = Color.WHITE,

    // == text-skip == //
    /** If true, then this scene always allows text skip. */
    public val alwaysAllowTextSkip: Boolean = false,
    /** If true, then this scene allows text skip when it is marked as seen. */
    public val enableTextSkipOnSeen: Boolean = true,
    /** If not-null, then this scene only allows text skip when the specified event flag is set. */
    public val enableTextSkipEventFlag: String? = null,
    /** If [enableTextSkipEventFlag] is set, this is the value that the flag must be. */
    public val enableTextSkipFlagValue: Int = 1,
    // == backgrounds == //
    /**
     * If set, this is used for the background colour. Otherwise, uses the default "flowy"
     * background.
     */
    public val backgroundColour: Color? = null,
    /** If set, this is used for the background. Otherwise, uses [backgroundColour]. */
    public val backgroundName: String? = null,
    /** If true, this scene will fade-in from the previous one. */
    public val causesFadeIn: Boolean = false,
    /**
     * If true, renders this scene in text-only mode. This changes the behaviour of certain other
     * parameters:
     *
     * - [textOnlyModeColour] is used instead of [defaultTextColour].
     * - If [backgroundColour] and [backgroundName] are null, then the background will be black.
     * - [drawPageButtons] is ignored.
     */
    public val textOnlyMode: Boolean = false,

    // == misc == //
    /**
     * If true, page buttons will be drawn. Useful for "dynamic" scenes.
     */
    public val drawPageButtons: Boolean = false,

    /**
     * If true, this scene is considered a non-renderable maintenance scene. These scenes cannot
     * have text pages but do not incur the resource creation penalty (such as of the spritebatch)
     * that regular scenes incur.
     */
    public val nonRenderable: Boolean = false,

    /**
     * The text at the top of the screen to draw.
     */
    public val topText: String? = null,
)