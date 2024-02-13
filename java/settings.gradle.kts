rootProject.name = "hack-forge-java"
include("wc")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val mockitoVersion = "5.10.0"
            val junitVersion = "5.10.2"

            library("mockito", "org.mockito:mockito-core:$mockitoVersion")

            library("junit5-api", "org.junit.jupiter:junit-jupiter-api:$junitVersion")
            library("junit5-engine", "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            library("junit5-params", "org.junit.jupiter:junit-jupiter-params:$junitVersion")
            bundle("junit5", listOf("junit5-api", "junit5-engine", "junit5-params"))
        }
    }
}
