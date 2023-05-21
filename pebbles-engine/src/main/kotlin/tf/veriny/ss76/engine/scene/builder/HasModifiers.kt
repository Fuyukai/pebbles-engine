/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.scene.builder

import tf.veriny.ss76.engine.scene.SceneModifiers

/**
 * Base interface for any scene builder that has modifiers.
 */
public interface HasModifiers {
    /** The current modifiers for this scene builder. */
    public var modifiers: SceneModifiers
}

public fun HasModifiers.fadeIn() {
    modifiers = modifiers.copy(causesFadeIn = true)
}