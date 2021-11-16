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

package tf.veriny.ss76

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import org.lwjgl.glfw.GLFW
import tf.veriny.ss76.engine.SceneRegistrar

/**
 * Launches the SS76 engine.
 *
 * @param namespace: The namespace to use for saved data.
 * @param registrar: The scene registration object.
 * @param windowTitle: The title to use for the window. Defaults to 'Pebbles Engine'.
 */
public fun launchEngine(
    namespace: String,
    registrar: SceneRegistrar,
    windowTitle: String = "Pebbles Engine",
    defaultTopText: String = "PEBBLES ENGINE"
) {

    val babyScreen = run {
        System.getProperty("is-baby-screen", "false").toBooleanStrict() || run {
            GLFW.glfwInit()
            val monitor = GLFW.glfwGetPrimaryMonitor()
            val res = GLFW.glfwGetVideoMode(monitor)
            res!!.height() < 960
        }
    }
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle(windowTitle)
        if (babyScreen) {
            setWindowedMode(800, 600)
        } else {
            setWindowedMode(1280, 960)
        }

        setResizable(false)
        setWindowIcon("icon-128x128.png")
        useVsync(true)
        setIdleFPS(60)
        setForegroundFPS(60)
    }

    Lwjgl3Application(SS76(namespace, registrar, defaultTopText), config)
}
