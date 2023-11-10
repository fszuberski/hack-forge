plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "com.fszuberski"
version = "1.0-SNAPSHOT"

val kotestVersion = "5.6.2"

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}