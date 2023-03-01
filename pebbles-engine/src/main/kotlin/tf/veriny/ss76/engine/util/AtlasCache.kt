/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.util

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion

/**
 * Caches the results of ``findRegion`` calls.
 */
public class AtlasCache(public val atlas: TextureAtlas) {
    private val cached = mutableMapOf<String, AtlasRegion>()

    /**
     * Gets a single region of the atlas, and caches it for future lookups.
     */
    public fun region(name: String): AtlasRegion {
        if (name in cached) return cached[name]!!

        val region = atlas.findRegion(name)
                     ?: throw IllegalArgumentException("no such region '$name'")
        cached[name] = region
        return region
    }
}