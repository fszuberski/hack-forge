rootProject.name = "hack-forge-java"
include("bookstore")
include("wc")
include("grind")
include("urlshortener")
include("multithreading")
include("interview-rate-limiter")
include("interview-money-transfer")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val commonsCodecVersion = "1.16.1"
            val commonsLangVersion = "3.14.0"
            val mockitoVersion = "5.24.0"
            val junitVersion = "5.10.2"
            val junitPlatformVersion = "1.10.2"
            val lombokVersion = "1.18.48"

            library("commons-codec", "commons-codec:commons-codec:$commonsCodecVersion")
            library("commons-lang3", "org.apache.commons:commons-lang3:$commonsLangVersion")

            library("lombok", "org.projectlombok:lombok:$lombokVersion")

            library("mockito", "org.mockito:mockito-core:$mockitoVersion")

            library("junit5-api", "org.junit.jupiter:junit-jupiter-api:$junitVersion")
            library("junit5-engine", "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            library("junit5-params", "org.junit.jupiter:junit-jupiter-params:$junitVersion")
            library("junit-platform-launcher", "org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
            bundle("junit5", listOf("junit5-api", "junit5-engine", "junit5-params"))
        }
    }
}
