/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm

/**
 * A single fragment in a scene.
 *
 * Fragments allow building up a scene progressively, potentially depending on previous choices.
 */
@JvmInline
public value class PsmSceneFragment(public val content: String) {
    public fun asInstant(): PsmSceneFragment {
        return PsmSceneFragment("$[instant=] $content $[pop=]")
    }
}