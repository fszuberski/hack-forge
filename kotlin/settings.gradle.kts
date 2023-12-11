rootProject.name = "hack-forge-kotlin"
include("coroutines")
include("functional-programming")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val coroutinesVersion = "1.7.3"
            val junitVersion = "5.10.1"
            val kotestVersion = "5.8.0"
            library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

            library("junit5-api", "org.junit.jupiter:junit-jupiter-api:$junitVersion")
            library("junit5-engine", "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            bundle("junit5", listOf("junit5-api", "junit5-engine"))

            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:$kotestVersion")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:$kotestVersion")
            library("kotest-property", "io.kotest:kotest-property:$kotestVersion")
            bundle("kotest", listOf("kotest-runner-junit5", "kotest-assertions-core", "kotest-property"))
        }
    }
}
