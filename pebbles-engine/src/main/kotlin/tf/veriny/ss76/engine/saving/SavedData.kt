/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.saving

import tf.veriny.ss76.EngineState
import tf.veriny.ss76.engine.scene.SceneState
import kotlin.reflect.KClass

/**
 * Helper class for managing non-engine persistent state.
 */
private class SavedData<What : Saveable>(
    val id: String,
    val type: KClass<What>,
    val instance: What
) : Saveable by instance {
    fun register(state: EngineState) {
        state.saveManager.addSaveable(id, this)
    }
}

/**
 * Registers a [Saveable] with the engine.
 */
public fun <T : Saveable> EngineState.registerSavedData(
    id: String, type: KClass<T>, instance: T
) {
    val data = SavedData(id, type, instance)
    data.register(this)
}

/**
 * Registers a [Saveable] with the engine.
 */
public inline fun <reified T : Saveable> EngineState.registerSavedData(
    id: String, block: () -> T
) {
    val instance = block()
    registerSavedData(id, T::class, instance)
}


/**
 * Gets the data for the specified [klass] in the EngineState.
 */
public fun <T : Saveable> EngineState.getData(klass: KClass<T>): T {
    return saveManager.getAllSubsystems()
        .filterIsInstance<SavedData<T>>()
        .find { it.type == klass }
        ?.instance
        ?: error("No such saveable '${klass.simpleName}' registered")
}

/**
 * Gets the data for the specified [T] in the EngineState.
 */
public inline fun <reified T : Saveable> EngineState.getData(): T {
    return getData(T::class)
}

/**
 * Shortcut function for [EngineState.getData] inside scene handlers.
 */
public inline fun <reified T : Saveable> SceneState.getData(): T =
    engineState.getData<T>()