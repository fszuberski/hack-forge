plugins { java }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories { mavenCentral() }

dependencies {
    testImplementation(libs.bundles.junit5)
    testImplementation(libs.mockito)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test { useJUnitPlatform() }
