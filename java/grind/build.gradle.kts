plugins {
    id("java")
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
    implementation(libs.bundles.junit5)
    implementation(libs.mockito)
}

tasks.test {
    useJUnitPlatform()
}
