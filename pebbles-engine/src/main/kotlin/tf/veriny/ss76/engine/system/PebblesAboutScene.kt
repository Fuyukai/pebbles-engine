/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.system

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.Button
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

        page {
            if (gitProperties.isEmpty) {
                addRawFragment("$[@=red] Uh oh!")
                return@page
            }

            addRawFragment("""
                #[@=green] Pebbles #[@=pink] Engine #[@=AFEEEE] ${gitProperties["git.build.version"]}
                (Branch: $[@=sky,chomp=true] ${gitProperties["git.branch"]} $[pop=1]) #[nl=2] 
                
                $[@=green] Git Commit: $[pop=] ${gitProperties["git.commit.id"]} $[nl=1]
                $[@=green] Git Commit Time: $[pop=] ${gitProperties["git.commit.time"]} $[nl=1]
                $[@=green] Git Commit Message: $[pop=] $[left-margin=21] '${gitProperties["git.commit.message.short"]}' $[pop=]
                $[nl=2]
                
                $[@=sky,left-margin=26] Baked with PSM 2.0 $[pop=1,nl=2]
                
                Powered by #[@=salmon,`=libgdx] LibGDX and #[@=salmon,`=lwjgl3] LWJGL3
            """.trimIndent())
        }
    }
}
