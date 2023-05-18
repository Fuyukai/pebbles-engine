/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.scene.SceneState

/**
 * A single *included* fragment in a scene. An includerd fragment is a fragment that may or may
 * not be shown instantly (that is, the frame counter doesn't tick up); or it may simply not
 * be shown at all.
 */
public class PsmIncludedFragment(
    public val fragment: PsmSceneFragment,
    public val isInstant: Boolean,
    public val condition: ((EngineState) -> Boolean)? = null
) {
    public fun asInstant(): String {
        return fragment.asInstant().content
    }

    public fun isStatic(): Boolean {
        return condition == null
    }

    public fun shouldBeIncluded(state: EngineState): Boolean {
        return condition?.invoke(state) != false
    }
}