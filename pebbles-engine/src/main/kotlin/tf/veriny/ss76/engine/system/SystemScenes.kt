/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.system

import tf.veriny.ss76.engine.ChangeSceneButton
import tf.veriny.ss76.engine.PushSceneButton
import tf.veriny.ss76.engine.scene.SceneManager
import tf.veriny.ss76.engine.scene.createAndRegisterScene

public const val SYSTEM_STARTUP_NAME: String = "engine.system-startup"

/**
 * Registers a handful of system maintenance scenes. These are generated at boot up.
 */
public fun registerSystemScenes(loadScene: String, sceneManager: SceneManager): Unit = sceneManager.let { sm ->
    sm.createAndRegisterScene(SYSTEM_STARTUP_NAME) {
        alwaysAllowTextSkip()
        enablePagination()

        val chainLoadSceneExists = sm.doesSceneExist(loadScene)
        val iDontCare = System.getProperty("i-dont-care", "false")
                            .toBooleanStrictOrNull() ?: false
        var fatal = !chainLoadSceneExists

        // calc dangling buttons
        val dangling = mutableListOf<String>()
        for (scene in sm.registeredScenes.values) {
            val buttons = scene.buttons.filter { it ->
                it.value is ChangeSceneButton || it.value is PushSceneButton
            }

            for (button in buttons) {
                val linkedScene = button.value.linkedId
                if (linkedScene == null) {
                    dangling += "Button ${button.key} in ${scene.sceneId} is a scene change button, but has " +
                                ":push:@red@ no linked scene ID :pop:\n"
                } else {
                    if (!sm.doesSceneExist(linkedScene)) {
                        dangling += "Button ${button.key} in ${scene.sceneId} references non-existent " +
                                    "scene: @salmon@$linkedScene\n"
                    }
                }
            }
        }

        onLoad {
            it.timer = 9999999

            if (iDontCare || (dangling.isEmpty() && !fatal)) {
                sm.changeScene(loadScene)
            }
        }

        page {
            raw(":push:¬instant¬ :newline-linger:false ")
            if (fatal) {
                line(":push:@red@ Fatal error when loading the engine! :pop:")
            } else {
                line(":push:@yellow@ Scene chain has errors! :pop:")
                line("This may not be fatal, but this is still a bug.")
                line("Re-run with -Di-dont-care=true to skip this.")
            }

            line("Dangling scenes: ${dangling.size}")
            newline()

            if (chainLoadSceneExists) {
                changeSceneButton(loadScene, "Ignore warnings and load scene $loadScene")
            } else {
                line("Scene '${loadScene}' does not exist!")
            }
            raw(":pop:")
        }

        for (i in dangling.chunked(5)) {
            page {
                raw(":push:¬instant¬ :newline-linger:false ")
                for (item in i) {
                    line(item)
                }
                raw(":pop:")
            }
        }
    }
}
