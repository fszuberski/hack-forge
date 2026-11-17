allprojects {
    // Kotlin's NIO session files need an absolute path when a Gradle daemon is reused.
    extra["kotlin.project.persistent.dir"] = rootProject.file(
        providers.gradleProperty("kotlin.project.persistent.dir").get()
    ).absolutePath
}
