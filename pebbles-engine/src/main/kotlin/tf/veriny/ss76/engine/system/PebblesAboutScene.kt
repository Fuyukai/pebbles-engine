/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.system

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.Button
import tf.veriny.ss76.engine.psm.rawPage
import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.register
import java.awt.Desktop
import java.net.URI
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
private val gitProperties = Properties().apply {
    val stream = SS76::class.java.getResourceAsStream("/git.properties")
    if (stream == null) {
        println("uh oh!")
    } else {
        stream.use { load(it) }
    }
}

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

/**
 * Registers the Pebbles Engine about scene.
 */
internal fun SceneManager.registerAboutScene() {
    register("engine.pebbles.about") {
        addButton(OpenBrowserButton("libgdx", "https://libgdx.com/"))
        addButton(OpenBrowserButton("lwjgl3", "https://www.lwjgl.org/"))

        rawPage {
            if (gitProperties.isEmpty) {
                rline("$[@=red] Uh oh!")
                return@rawPage
            }

            rline("#[@=green] Pebbles #[@=pink] Engine #[@=AFEEEE] ${gitProperties["git.build.version"]} ")
            rline("(Branch: \$[@=sky,chomp=true] ${gitProperties["git.branch"]} \$\$)")
            nl(2)

            nlline("\$[@=green] Git Commit: \$\$ ${gitProperties["git.commit.id"]}")
            nlline("\$[@=green] Git Commit Time: \$\$ ${gitProperties["git.commit.time"]}")
            rline("\$[@=green] Git Commit Message: \$\$ \$[left-margin=21] '${gitProperties["git.commit.message.short"]}' \$\$")
            nl(2)

            nlline("\$[@=sky,left-margin=26] Baked with PSM 2.0 \$\$")
            nl(2)

            rline("Powered by #[@=salmon,`=libgdx] LibGDX and #[@=salmon,`=lwjgl3] LWJGL3")
        }
    }
}
