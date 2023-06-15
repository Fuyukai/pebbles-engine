/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.pebbles

import tf.veriny.ss76.*

public object MakeUp {

    @JvmStatic
    public fun main(args: Array<String>) {
        val settings = SS76Settings(
            namespace = "pebbles-test-project",
            initialiser = ::setupEngine,
            isDeveloperMode = isDeveloperMode(MakeUp::class)
        )
        SS76.start(settings)
    }

    public fun setupEngine(state: EngineState) {
        state.sceneManager.registerDemoScenes()
    }
}