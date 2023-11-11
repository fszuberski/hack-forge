rootProject.name = "hack-forge-kotlin"
include("coroutines")
include("functional-programming")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotestVersion = "5.8.0"
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:$kotestVersion")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:$kotestVersion")
            library("kotest-property", "io.kotest:kotest-property:$kotestVersion")
            bundle("kotest", listOf("kotest-runner-junit5", "kotest-assertions-core", "kotest-property"))
        }
    }
}
