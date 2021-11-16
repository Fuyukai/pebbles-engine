Pebbles Engine
==============

Pebbles is the engine powering my (as of yet unreleased) Virtual Novel, Signalling System 76.

The code here is in a very rough shape, there's little to no documentation on the parser-lexer
used for scenes, or really any documentation.

Everything here is under the GPLv3, however for current legal reasons I cannot accept code-related
PRs for the time being, unless you attribute copyright to me.

Pebbles is named in honour of my beloved kitty cat.

Usage
-----

Add my maven:

.. code-block:: kotlin

    repositories {
        maven(url="https://maven.veriny.tf")
    }

Then add the library to your project:

.. code-block:: kotlin

    dependencies {
        implementation("tf.veriny.pebbles:pebbles-engine:x.x.x")
    }

Then all code is under the ``tf.veriny.ss76.engine`` package. (Naming is inconsistent because my
package is