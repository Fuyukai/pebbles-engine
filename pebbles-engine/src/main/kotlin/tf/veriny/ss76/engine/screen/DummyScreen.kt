/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.screen

import ktx.app.clearScreen

public object DummyScreen : Screen {
    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f)
    }

    override fun dispose() {

    }
}