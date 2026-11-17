plugins {
    id("java")
}

group = "com.fszuberski"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.junit5)
    testImplementation(libs.mockito)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Main-Class" to "com.fszuberski.wc.WordCountApplication"
        ))
    }
}

tasks.test {
    useJUnitPlatform()
}
