/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.screen

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ktx.app.clearScreen
import ktx.freetype.generateFont
import tf.veriny.ss76.EngineState
import tf.veriny.ss76.SS76
import tf.veriny.ss76.engine.util.EktFiles
import tf.veriny.ss76.engine.util.NioFileHandle
import tf.veriny.ss76.use
import java.lang.management.ManagementFactory
import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

/**
 * The screen for rendering errors.
 */
public class ErrorScreen(
    private val state: EngineState?,
    public val error: Throwable,
) : Screen {
    private var hasPrinted = false

    private val batch = SpriteBatch()
    private val emergencyFont = run {
        val path = EktFiles.RESOLVER.getPath("engine/fonts/Mx437_Wang_Pro_Mono.ttf")
            ?: error("what the fuck!")

        val generator = FreeTypeFontGenerator(
            NioFileHandle(path, Files.FileType.Classpath)
        )
        generator.generateFont { size = 16; color = Color.WHITE; mono = true }
    }

    private val live = run {
        val path = EktFiles.RESOLVER.getPath("gfx/live.png")
            ?: error("what the fuck! pt 2!")
        Texture(NioFileHandle(path, Files.FileType.Classpath))
    }

    private val message = run {
        val baseMessage = if (state == null)
            "Fatal error when loading SS76 engine!"
        else "Fatal error during rendering!"

        val fullMessage = StringBuilder()
        fullMessage.append(baseMessage)
        fullMessage.append("\n\n")

        fullMessage.append(error.stackTraceToString())
        fullMessage.append("\n\n")

        val managementBean = ManagementFactory.getRuntimeMXBean()
        fullMessage.append("JVM: ${managementBean.vmVersion}\n")
        fullMessage.append("JVM name: ${managementBean.vmName}\n")
        fullMessage.append("JVM vendor: ${managementBean.vmVendor}\n")

        val args = managementBean.inputArguments.joinToString(" ")
        fullMessage.append("JVM Args: '${args}'\n")
        fullMessage.append('\n')

        if (state == null) {
            fullMessage.append("Developer mode: Unsure\n")
        } else {
            fullMessage.append("Developer mode: ${state.settings.isDeveloperMode}\n")
        }

        for ((key, value) in System.getProperties()) {
            if (key.toString().startsWith("ss76.")) {
                fullMessage.append("-D${key}: '$value'\n")
            }
        }

        fullMessage.append("\n")

        val memoryBean = ManagementFactory.getMemoryMXBean()
        val heap = memoryBean.heapMemoryUsage
        fullMessage.append("Memory usage: ${heap.used} / ${heap.max}\n")
        val offHeap = memoryBean.nonHeapMemoryUsage
        fullMessage.append("Off-heap usage: ${offHeap.used} / ${offHeap.max}\n")
        fullMessage.append("\n")

        if (SS76.GIT_PROPERTIES.isEmpty()) {
            fullMessage.append("Unable to gather engine internal info!\n\n")
        } else {
            fullMessage.append("Engine version: ${SS76.GIT_PROPERTIES["git.build.version"]}\n")
            fullMessage.append("Engine commit: ${SS76.GIT_PROPERTIES["git.commit.id"]}\n")
        }

        if (state != null) {
            val currentScreen = state.screenManager.currentScreen
            fullMessage.append("Current screen: ${currentScreen.javaClass.simpleName}\n")

            if (currentScreen is ShimScreen) {
                fullMessage.append("Before: ${currentScreen.previousScreen.javaClass.simpleName}\n")
                fullMessage.append("Current: ${currentScreen.currentScreen.javaClass.simpleName}\n")
            } else if (currentScreen is FadeInScreen) {
                fullMessage.append("Out: ${currentScreen.previousScreen.javaClass.simpleName}\n")
                fullMessage.append("In: ${currentScreen.newScreen.javaClass.simpleName}\n")
                fullMessage.append("State: ${currentScreen.fadeInState}\n")
            }
            fullMessage.append("\n")

            if (state.sceneManager.stackSize > 0) {
                fullMessage.append("Scenes:\n")

                for ((idx, scene) in state.sceneManager.sceneStack.withIndex()) {
                    fullMessage.append("$idx: ${scene.definition.sceneId}\n")
                }
            }
        } else {
            fullMessage.append(
                "Error occurred during engine startup, unable to gather detailed engine info.\n"
            )
        }

        fullMessage.toString()
    }

    init {
        state?.musicManager?.stop()
    }

    override fun render(delta: Float) {
        clearScreen(255f, 0f, 0f, 0f)

        if (!hasPrinted) {
            println(message)
            val formattedDate = ZonedDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val filename = "fatal-crash-${formattedDate}.txt"
            val path = Path.of(".").resolve(filename)

            try {
                path.createParentDirectories()
                path.writeText(message)
            } catch (_: Exception) {}

            hasPrinted = true
        }

        batch.use {
            emergencyFont.draw(
                    this,
                    message,
                    1f,
                    Gdx.graphics.height - 10f
                )
        }
    }

    override fun dispose() {

    }
}
