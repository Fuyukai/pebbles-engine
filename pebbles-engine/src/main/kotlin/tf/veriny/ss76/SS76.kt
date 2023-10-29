/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.FPSLogger
import ktx.app.KtxApplicationAdapter
import tf.veriny.ss76.engine.PreferencesManager
import tf.veriny.ss76.engine.screen.ErrorScreen
import tf.veriny.ss76.engine.util.EktFiles
import tf.veriny.ss76.engine.util.EktLogger
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Dummy [KtxApplicationAdapter] implementation that simply passes everything
 */
@OptIn(ExperimentalTime::class)
public class SS76(
    private val settings: SS76Settings,
) : KtxApplicationAdapter {
    public companion object {
        public fun start(settings: SS76Settings) {
            val res = Lwjgl3ApplicationConfiguration.getDisplayMode()

            val config = Lwjgl3ApplicationConfiguration().apply {
                setTitle(settings.windowTitle)

                val forceScreenSize = System.getProperty("ss76.force-screen-size")
                if (forceScreenSize != null) {
                    val (width, height) = forceScreenSize.split('x').map { it.toInt() }
                    setWindowedMode(width, height)
                } else {
                    when {
                        res.height <= 960 -> {
                            setWindowedMode(800, 600)
                        }

                        res.height <= 1080 -> {
                            setWindowedMode(1280, 960)
                        }

                        else -> {
                            setWindowedMode(1440, 1080)
                        }
                    }
                }

                setResizable(false)
                setWindowIcon(settings.iconName)
                useVsync(true)
                setIdleFPS(60)
                setForegroundFPS(60)
            }

            val engine = SS76(settings)
            Lwjgl3Application(engine, config)
        }

        public val GIT_PROPERTIES: Map<String, String> = Properties().let {
            val stream = SS76::class.java.getResourceAsStream("/git.properties")
            if (stream == null) {
                println("uh oh!")
                emptyMap()
            } else {
                stream.reader(Charsets.UTF_8).let { s -> it.load(s) }
                it.toMap() as Map<String, String>
            }
        }
    }

    private lateinit var state: EngineState
    private var loadingErrorScreen: ErrorScreen? = null
    private var renderTime = 0L

    override fun create() {
        Gdx.files = EktFiles
        Gdx.app.applicationLogger = EktLogger

        try {
            createImpl()
        } catch (e: Exception) {
            val error = ErrorScreen(null, e)
            loadingErrorScreen = error
        }
    }

    private fun createImpl() {
        // extremely early init, find and load language code
        val prefs = PreferencesManager(settings)
        prefs.loadPreferences()

        val potentialPropLang = System.getProperty("ss76.lang")
        if (potentialPropLang != null) {
            prefs["language"] = potentialPropLang
            prefs.save()
        }

        state = EngineState(settings, prefs)
        state.created()

        Gdx.input.inputProcessor = state.input
    }

    private val fps = FPSLogger()

    private fun debugRender() {
        val time = measureTime { state.render() }.inWholeNanoseconds
        renderTime += time
    }

    override fun render() {
        val err = loadingErrorScreen
        if (err != null) {
            return err.render(Gdx.graphics.deltaTime)
        }

        state.render()
    }
}
