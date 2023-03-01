Pebbles Engine
==============

Pebbles is a virtual novel engine written in Kotlin.

The code here is in a very rough shape, there's little to no documentation on the parser-lexer
used for scenes, or really any documentation.

Everything here is licensed under the MPL 2.0.

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

Then all code is under the ``tf.veriny.ss76.engine`` package. It's named that because this was
originally the VN engine in use for an old project that I extracted the codee out of.