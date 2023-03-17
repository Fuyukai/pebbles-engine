/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.system

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.createAndRegisterScene
import java.awt.Desktop
import java.net.URI

private class OpenBrowserButton(override val name: String, private val link: String) : Button {
    override fun run(state: EngineState) {
        val os = System.getProperty("os.name").lowercase()

        if (os == "linux") {
            Runtime.getRuntime().exec("xdg-open $link".split(" ").toTypedArray())
        } else if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            desktop.browse(URI(link))
        }
    }
}

public fun registerOSSCredits(sm: SceneManager) {
    sm.createAndRegisterScene("engine-oss-credits") {
        page {
            line("=== Resources Used ===")
            newline()

            line(
                "Pebbles uses resources from the :push:`ofp`@green@ Oldschool Font Pack :pop: " +
                "which is licenced under the Creative Commons Attribution-ShareAlike 4.0 International " +
                "License."
            )
            newline()

            line("Pebbles is based on the `gdx`@green@LibGDX game engine.")
            newline()

            addButton(OpenBrowserButton("ofp", "https://int10h.org/oldschool-pc-fonts/"))
            addButton(OpenBrowserButton("gdx", "https://github.com/libgdx/libgdx"))
        }
    }
}
