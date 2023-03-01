/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

import com.badlogic.gdx.audio.Music
import tf.veriny.ss76.EngineState

/**
 * Handles starting and stopping music.
 */
public class MusicManager(private val state: EngineState) {
    public companion object {
        public val NO_MUSIC: Boolean = System.getProperty("ss76.no-music", "false").toBooleanStrict()
    }

    /** If music is currently being played. */
    public var playing: Boolean = false
        private set

    private var currentName = ""
    private var currentTrack: Music? = null

    private fun startPlaying(
        track: Music, loop: Boolean = false,
        completion: ((Music) -> Unit)? = null,
    ) {
        playing = true
        if (currentTrack != null) currentTrack!!.stop()

        if (completion != null) {
            track.setOnCompletionListener(completion)
        }

        track.isLooping = loop
        currentTrack = track
        track.play()
    }

    /**
     * Starts playing a track.
     */
    public fun startTrack(name: String) {
        if (NO_MUSIC) return
        // don't restart currently playing music
        if (currentName == name) return

        val intro = state.assets.tryGetMusicFile("$name-intro")
        val looper = state.assets.tryGetMusicFile(name) ?: error("no such music: $name")

        if (intro == null) {
            startPlaying(looper, loop = true)
        } else {
            // might want to try and crossfade this, as there is a TINY gap
            startPlaying(intro, loop = false) {
                startPlaying(looper, loop = true)
            }
        }

        currentName = name
    }

    /**
     * Stops playing music.
     */
    public fun stop() {
        currentTrack?.stop()
        currentTrack = null
        playing = false
    }
}