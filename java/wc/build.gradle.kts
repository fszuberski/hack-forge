plugins {
    id("java")
}

group = "com.fszuberski"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.junit5)
}

tasks.test {
    useJUnitPlatform()
}