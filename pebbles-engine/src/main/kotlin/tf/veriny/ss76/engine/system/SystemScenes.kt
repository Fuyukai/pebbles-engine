/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.system

import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.ChangeSceneButton
import tf.veriny.ss76.engine.PushSceneButton
import tf.veriny.ss76.engine.SceneManager
import tf.veriny.ss76.engine.scene.createAndRegisterScene

/**
 * Registers a handful of system maintenance scenes. These are generated at boot up.
 */
public fun registerSystemScenes(loadScene: String, sceneManager: SceneManager): Unit = sceneManager.let { sm ->
    sm.createAndRegisterScene("system-startup-scene") {
        // calc dangling buttons
        val dangling = mutableListOf<String>()
        for (scene in sm.registeredScenes.values) {
            val buttons = scene.buttons.filter { it ->
                it.value is ChangeSceneButton || it.value is PushSceneButton
            }

            for (button in buttons) {
                val linkedScene = button.value.linkedId
                if (linkedScene == null) {
                    dangling += "Button ${button.key} in ${scene.id} is a scene change button, but has " +
                                ":push:@red@ no linked scene ID :pop:\n"
                } else {
                    val actualScene = sm.registeredScenes[linkedScene]
                    if (actualScene == null) {
                        dangling += "Button ${button.key} in ${scene.id} references non-existent " +
                                    "scene: @salmon@$linkedScene\n"
                    }
                }
            }
        }

        onLoad {
            if (dangling.isEmpty()) { sm.changeScene(loadScene) }
        }

        page {
            line(":push:@green@ SS76 Engine Startup Scene :pop:")
            newline()

            line("Dangling scenes: ${dangling.size}")
            newline()

            changeSceneButton(loadScene,"Ignore warnings and load scene $loadScene")
        }

        for (i in dangling.chunked(5)) {
            page {
                for (item in i) {
                    line(item)
                }
            }
        }
    }
}