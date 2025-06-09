pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://chaquo.com/maven") // Added for Chaquopy, matching your shorthand style
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")       // Your original JitPack declaration
        maven("https://chaquo.com/maven") // Added for Chaquopy, matching your shorthand style
    }
}

rootProject.name = "HealthMateApplication"
include(":app")