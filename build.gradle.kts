import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessTask
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.8.21").apply(false)
    id("com.diffplug.spotless").version("6.18.0").apply(false)
    id("com.github.ben-manes.versions").version("0.46.0")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        // TODO: rehost shape drawer on our maven for now...
        maven(url = "https://jitpack.io")
    }
}

subprojects {
    group = "tf.veriny.pebbles"

    apply(plugin = "kotlin")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.ben-manes.versions")

    val implementation by configurations
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(20))
        }
    }

    configure<SpotlessExtension> {
        kotlin {
            targetExclude("build/generated/**")
            licenseHeaderFile(project.file("LICENCE-HEADER"))
        }
    }

    configure<KotlinJvmProjectExtension> {
        explicitApi = ExplicitApiMode.Strict
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xjvm-default=all",  // Forcibly enable Java 8+ default interface methods
                    "-Xassertions=always-enable",  // Forcibly enable assertions
                    "-Xlambdas=indy",  // Forcibly use invokedynamic for all lambdas.
                )
                jvmTarget = "19"
                languageVersion = "1.9"
            }
        }

        filter { it.name.startsWith("spotless") }
            .forEach { it.group = "lint" }

        withType<JavaCompile> {
            targetCompatibility = "19"
        }
    }
}