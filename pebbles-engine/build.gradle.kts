plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

version = "0.8.0"

// can't be private due to insane inscrutible gradle bugs
@Suppress("PropertyName")
val KTX_VERSION = "1.11.0-rc3"
@Suppress("PropertyName")
val GDX_VERSION = "1.11.0"

dependencies {
    api("com.badlogicgames.gdx:gdx:$GDX_VERSION")
    api("com.badlogicgames.gdx:gdx-freetype:$GDX_VERSION")
    api("io.github.libktx:ktx-log:$KTX_VERSION")
    api("io.github.libktx:ktx-freetype:$KTX_VERSION")
    api("io.github.libktx:ktx-app:$KTX_VERSION")
    api("io.github.libktx:ktx-math:$KTX_VERSION")
    api("io.github.libktx:ktx-assets:$KTX_VERSION")
    api("io.github.libktx:ktx-graphics:$KTX_VERSION")

    api("com.squidpony:squidlib-util:3.0.6")

    api("com.badlogicgames.gdx:gdx-backend-lwjgl3:$GDX_VERSION")
    implementation("com.badlogicgames.gdx:gdx-platform:$GDX_VERSION:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:$GDX_VERSION:natives-desktop")

    api("dev.dirs:directories:26")
    api("com.squareup.okio:okio:3.3.0")

    api("com.fasterxml.jackson.core:jackson-core:2.14.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.0")

}


publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])

        pom {
            name.set(project.name)
            description.set("The Pebbles virtual novel engine")
            url.set("https://vnengine.veriny.tf")

            licenses {
                license {
                    name.set("MPL-2.0")
                    url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                }
            }

            developers {
                developer {
                    id.set("Fuyukai")
                    name.set("Lura Skye")
                    url.set("https://veriny.tf")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://maven.veriny.tf/releases")

            credentials {
                username = project.properties["verinyUsername"] as? String
                password = project.properties["verinyPassword"] as? String
            }
        }
    }
}
