/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.ss76.engine.psm.stack

import com.badlogic.gdx.graphics.Color
import tf.veriny.ss76.engine.scene.TextualNode

/**
 * Base interface for the hierarchy of possible PSM scene state modifiers.
 */
public sealed interface PsmStateEntry<T : Any> {
    public val value: T
}

// specific subtypes


// boolean entries
/** Enables or disables newline linger. */
public class PsmNewlineLinger(override val value: Boolean) : PsmStateEntry<Boolean>

/** Chomps extra spaces after words. */
public class PsmChomp(override val value: Boolean) : PsmStateEntry<Boolean>

/** Controls instant mode (i.e. no frame counter increment.) */
public class PsmInstant(override val value: Boolean) : PsmStateEntry<Boolean>

/** Controls if buttons should have their colour based on the button's status. */
public class PsmColourButtonLink(override val value: Boolean) : PsmStateEntry<Boolean>

// core properties

/** A single colour change entry. */
public class PsmColour(override val value: Color) : PsmStateEntry<Color>

/** A single effect entry, containing multiple effects. */
public class PsmEffect(override val value: Set<TextualNode.Effect>) : PsmStateEntry<Set<TextualNode.Effect>>

/** A single button ID entry. */
public class PsmButton(override val value: String) : PsmStateEntry<String>

// margins
/** Controls the right-hand margin. */
public class PsmRightMargin(override val value: Int) : PsmStateEntry<Int>

/** Controls the left-hand margin. */
public class PsmLeftMargin(override val value: Int) : PsmStateEntry<Int>

// misc
/** The font to render the nodes with. */
public class PsmFont(override val value: String) : PsmStateEntry<String>

/** The number of frames to linger on a newline. */
public class PsmNewlineLingerFrames(override val value: Int) : PsmStateEntry<Int>

/** The number of linger frames to use after a node. */
public class PsmLingerFrames(override val value: Int) : PsmStateEntry<Int>

/** The number of frames between a node's beginning and end. */
public class PsmFramesPerWord(override val value: Int) : PsmStateEntry<Int>
