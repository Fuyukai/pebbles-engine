/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine

/**
 * An enumeration of the possible deactivation types.
 */
public enum class DeactivationType {
    /** This scene has been deactivated because a new scene has been pushed on top of it. */
    PUSHED,

    /** This scene has been deactivated because it has been popped from the scene stack. */
    POPPED,
}
