plugins {
    kotlin("jvm") version "1.9.20"
}

group = "com.fszuberski"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.bundles.kotest)
}