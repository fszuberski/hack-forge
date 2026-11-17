plugins {
    java
}

group = "com.fszuberski"
version = "1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.junit5)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.withType<Test>().configureEach {
    // Keep local study serial by default; bound opt-in concurrency even on large machines.
    val parallel = providers.gradleProperty("courseParallel").map(String::toBoolean).getOrElse(false)
    systemProperty("junit.jupiter.execution.parallel.enabled", parallel.toString())
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "2")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.max-pool-size", "2")
    systemProperty("course.projectDir", projectDir.absolutePath)
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.test {
    description = "Checks course integrity; does not run unfinished exercises."
    useJUnitPlatform {
        excludeTags("exercise")
    }
}

tasks.register<Test>("exerciseTest") {
    description = "Runs exercise acceptance tests (intentionally red until implemented). Use --tests to select one."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("exercise")
    }
    shouldRunAfter(tasks.test)
}

tasks.register<JavaExec>("diagnosticDemo") {
    description = "Runs a bounded monitor-contention demo for jcmd/thread-dump practice."
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.fszuberski.multithreading.diagnostics.ContendedServiceDemo"
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
