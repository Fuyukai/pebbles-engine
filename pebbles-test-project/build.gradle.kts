import org.lwjgl.Lwjgl
import org.lwjgl.Release
import org.lwjgl.lwjgl

plugins {
    id("org.lwjgl.plugin").version("0.0.34")
}

dependencies {
    implementation(project(":pebbles-engine"))

    // override libgdx's lwjgl3, at least until 3.3.2 is transitive via gdx-backend-lwjgl3
    // 3.3.1 doesn't work under intellij debugger on java 19+ due to JNI changes. 3.3.2 fixes this.
    lwjgl {
        version = Release.`3_3_2`
        implementation(
            Lwjgl.Module.glfw,
            Lwjgl.Module.openal,
            Lwjgl.Module.jemalloc,
            Lwjgl.Module.opengl,
            Lwjgl.Module.stb,
        )
    }
}