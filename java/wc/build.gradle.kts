plugins {
    id("java")
}

group = "com.fszuberski"
version = "0.9"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.junit5)
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Main-Class" to "com.fszuberski.wc.Main"
        ))
    }
}

tasks.test {
    useJUnitPlatform()
}