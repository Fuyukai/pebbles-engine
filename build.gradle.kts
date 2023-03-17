plugins {
    id("org.jetbrains.kotlin.jvm").version("1.8.20-RC").apply(false)
    id("com.diffplug.spotless").version("6.17.0").apply(false)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    group = "tf.veriny.pebbles"

    apply(plugin = "kotlin")
    apply(plugin = "com.diffplug.spotless")

    val implementation by configurations
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            targetExclude("build/generated/**")
            licenseHeaderFile(rootProject.file("gradle/LICENCE-HEADER"))
        }
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xopt-in=kotlin.RequiresOptIn",  // Enable @OptIn
                    "-Xstring-concat=indy-with-constants",  // Enable invokedynamic string concat
                    "-Xjvm-default=all",  // Forcibly enable Java 8+ default interface methods
                    "-Xassertions=always-enable",  // Forcibly enable assertions
                    "-Xlambdas=indy",  // Forcibly use invokedynamic for all lambdas.
                )
                jvmTarget = "19"
            }
        }
    }
}