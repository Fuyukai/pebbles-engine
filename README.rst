Pebbles Engine
==============

Pebbles is my Virtual Novel game engine, written in Kotlin. It's named after my cat, Pebbles.

This is the Okuu (0.8) branch, which contains significant rewrites over the old engine including
a better renderer, music, better font generation, and so on.

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
        implementation("tf.veriny.pebbles:pebbles-engine:0.8.x")
    }

Create an instance of ``SS76Settings``, and use ``SS76.start`` to run your game:

.. code-block:: kotlin

    public object MakeUp {

        @JvmStatic
        public fun main(args: Array<String>) {
            val settings = SS76Settings(
                namespace = "my-project-namespace",
                initialiser = ::setupEngine,
                isDebugMode = !isInsideJar(MakeUp::class)
            )
            SS76.start(settings)
        }

        public fun setupEngine(state: EngineState) {
            // do things here
        }
    }

All code is under the ``tf.veriny.ss76.engine`` package. It's named that because this was
originally the VN engine in use for an old project that I extracted the code out of.