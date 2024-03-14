plugins {
    id("java")
}

group = "com.fszuberski"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.junit5)
    implementation(libs.mockito)
}

tasks.test {
    useJUnitPlatform()
}