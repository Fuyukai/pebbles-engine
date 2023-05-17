/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76

/**
 * Contains the settings to load the engine with.
 */
public data class SS76Settings(
    /**
     * The save-data namespace to save your things in.
     */
    public val namespace: String,

    /**
     * A function to be called during engine creation that will set up the engine state.
     */
    public val initialiser: (EngineState) -> Unit,

    /**
     * The title text for the newly created window.
     */
    public val windowTitle: String = "Pebbles Engine",

    /**
     * The default language code to use for loading PSF files and other localised text.
     */
    public val defaultLanguageCode: String = "en",

    // == optional == //
    /**
     * The default text to draw at the top during NVL-renderer scenes.
     */
    public val defaultTopText: String = "PEBBLES ENGINE",

    /**
     * The application icon to use. Defaults to a photograph of my cat.
     */
    public val iconName: String = "gfx/icon-default.png",

    /**
     * If True, then the checkpoint button will be rendered. Otherwise, it will be skipped. The
     * checkpoint scene will still be accessible without this.
     */
    public val enableCheckpoints: Boolean = true,

    /**
     * If True, then additional debugging features will be enabled.
     */
    public val isDebugMode: Boolean = false,

    /**
     * The ID of the scene to push after engine initialisation. If this is null, then the default
     * scene (`main-menu`, or `demo-meta-menu` in debug mode) will be loaded instead.
     */
    public val startupScene: String? = null,
)