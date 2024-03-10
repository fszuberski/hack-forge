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
}

tasks.test {
    useJUnitPlatform()
}