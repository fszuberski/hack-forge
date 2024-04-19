rootProject.name = "hack-forge-java"
include("wc")
include("grind")
include("urlshortener")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val mockitoVersion = "5.10.0"
            val junitVersion = "5.10.2"
            val lombokVersion = "1.18.32"

            library("commons-codec", "commons-codec:commons-codec:1.16.1", )
            library("commons-lang3", "org.apache.commons:commons-lang3:3.14.0")

            library("lombok", "org.projectlombok:lombok:$lombokVersion")

            library("mockito", "org.mockito:mockito-core:$mockitoVersion")

            library("junit5-api", "org.junit.jupiter:junit-jupiter-api:$junitVersion")
            library("junit5-engine", "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            library("junit5-params", "org.junit.jupiter:junit-jupiter-params:$junitVersion")
            bundle("junit5", listOf("junit5-api", "junit5-engine", "junit5-params"))
        }
    }
}
