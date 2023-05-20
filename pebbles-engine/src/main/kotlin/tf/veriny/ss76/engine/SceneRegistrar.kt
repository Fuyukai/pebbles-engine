/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

/**
 * Interface for an end-user scene registrar.
 */
public fun interface SceneRegistrar {
    /**
     * Registers all the scenes in this registrar with the specified scene manager.
     */
    public fun register(sceneManager: SceneManager)
}