/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("SpellCheckingInspection")

package tf.veriny.ss76

import com.badlogic.gdx.Input.Keys.V
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import java.lang.management.ManagementFactory
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.random.Random
import kotlin.reflect.KClass


/**
 * Helper function to automatically begin and dispose a Batch.
 */
public inline fun <T : Batch, R> T.use(fn: T.() -> R): R {
    begin()
    try {
        return fn()
    } finally {
        end()
    }
}

/**
 * Helper function to automatically begin and dispose a ShapeRenderer.
 */
public inline fun <R> ShapeRenderer.use(type: ShapeRenderer.ShapeType, fn: ShapeRenderer.() -> R): R {
    begin(type)
    try {
        return fn()
    } finally {
        end()
    }
}

public fun Iterable<Boolean>.all(): Boolean {
    for (i in this) {
        if (!i) return false
    }

    return true
}

// this performs some checks so that you can't boot in dev mode outside of the IDE.
/**
 * Checks if the engine is running in developer mode. Your main object should be provided as
 * [usingKlass].
 */
public fun isDeveloperMode(usingKlass: KClass<*>? = null): Boolean {
    if (!System.getProperty("ss76.developer-mode", "false").toBooleanStrict()) return false

    // small number of heuristics available here.
    // 1) check if the jar provided isn't inside a jar.
    if (usingKlass != null && !isInsideJar(usingKlass)) return true

    // 2) check classpath for gradle caches
    val path = System.getProperty("java.class.path")
    for (entry in path.split(':')) {
        // running inside gradle, probably developer mode.
        if (entry.contains(".gradle/caches")) return true
    }

    // 3) check jvm args for the idea_rt java agent.
    val bean = ManagementFactory.getRuntimeMXBean()
    val args = bean.inputArguments
    for (arg in args) {
        if (!arg.startsWith("-javaagent")) continue
        // running using the intellij agent.
        if (arg.contains("idea_rt.jar")) return true
    }

    return false
}


/**
 * Checks if the specified class is inside a jar or not.
 */
public fun isInsideJar(usingKlass: KClass<*> = SS76::class): Boolean {
    val uri = usingKlass.java.getResource("${usingKlass.simpleName}.class")!!.toURI()
    return uri.scheme == "jar"
}

private val ALNUM = ('0'..'9') + ('A'..'Z') + ('a'..'z')
private val ALPHA = ('A'..'Z') + ('a'..'z')

public fun randomChar/*lotte*/(r: Random, numbers: Boolean = false): Char {
    return if (numbers) ALNUM.random(r)
    else ALPHA.random(r)
}

public fun randomString(r: Random, length: Int): String {
    return (0 until length).joinToString("") { ALPHA.random(r).toString() }
}

public fun ShapeRenderer.roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float) {
    // Central rectangle
    rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius)

    // Four side rectangles, in clockwise order
    rect(x + radius, y, width - 2 * radius, radius)
    rect(x + width - radius, y + radius, radius, height - 2 * radius)
    rect(x + radius, y + height - radius, width - 2 * radius, radius)
    rect(x, y + radius, radius, height - 2 * radius)

    // Four arches, clockwise too
    arc(x + radius, y + radius, radius, 180f, 90f)
    arc(x + width - radius, y + radius, radius, 270f, 90f)
    arc(x + width - radius, y + height - radius, radius, 0f, 90f)
    arc(x + radius, y + height - radius, radius, 90f, 90f)
}

private const val SAMPLE =
    "人初うの静女嫦既た最が、とがゲは存しきみて賢グこ怨子繰り言。" +
    "が被一者ィの様の、しれがじいエンいっを強らのををく都ははようとりー、怒殺てるににみみ、" +
    "のは夫娥、に息るだい部で、月彼純れうい返一ださっょた在。" +
    "をした怨って民しの歩怨だ持だ詳化とめに襲省月るデムン"

/**
 * Creates a fake mojibake effect.
 */
public fun String.mojibakify(rng: Random): String {
    val chars = StringBuilder()
    repeat(this.length) {
        chars.append(SAMPLE.random(rng))
    }

    val encoded = chars.toString().toByteArray(Charsets.UTF_8)
    // not efficient
    return encoded.toString(Charset.forName("Windows-1252"))
        .replace("\n", "")
        .replace("\r", "")
        .replace(" ", "")
}

public fun String.toBooleanHandleBlank(default: Boolean): Boolean {
    return if (isBlank()) default
    else toBooleanStrict()
}
