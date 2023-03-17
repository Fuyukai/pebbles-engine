plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

version = "0.7.2"

dependencies {
    api("com.badlogicgames.gdx:gdx:1.10.0")
    api("com.badlogicgames.gdx:gdx-freetype:1.10.0")
    api("io.github.libktx:ktx-log:1.10.0-b2")
    api("io.github.libktx:ktx-freetype:1.10.0-b2")
    api("io.github.libktx:ktx-app:1.10.0-b2")
    api("io.github.libktx:ktx-assets:1.10.0-b2")
    api("io.github.libktx:ktx-graphics:1.10.0-b2")

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.10.0")
    implementation("com.badlogicgames.gdx:gdx-platform:1.10.0:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.10.0:natives-desktop")

    api("dev.dirs:directories:26")
    api("com.squareup.okio:okio:2.10.0")

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
                    url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
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
