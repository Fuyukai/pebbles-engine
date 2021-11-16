/*
 * This file is part of Pebbles.
 *
 * Pebbles is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pebbles is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pebbles.  If not, see <https://www.gnu.org/licenses/>.
 */

package tf.veriny.ss76.engine.system

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.SceneManager
import tf.veriny.ss76.engine.scene.createAndRegisterScene
import java.awt.Desktop
import java.net.URI

private class OpenBrowserButton(override val name: String, private val link: String) : Button {
    override fun run(state: EngineState) {
        val os = System.getProperty("os.name").lowercase()

        if (os == "linux") {
            Runtime.getRuntime().exec("xdg-open $link")
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