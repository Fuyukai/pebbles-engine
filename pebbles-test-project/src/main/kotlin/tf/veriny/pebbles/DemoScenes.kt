/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.pebbles

import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.register
import tf.veriny.ss76.engine.scene.sceneSequence

public fun SceneManager.registerDemoScenes(): Unit = sceneSequence {
    modifiers = modifiers.copy(drawPageButtons = true)

    register("demo-meta-menu")
    register("demo.colours") {
        page("demo.colours.1")
        page("demo.colours.2")
    }
    register("demo.effects")
    register("demo.font-changes")
    register("demo.frame-counters")

    register("demo.dialogue")
}