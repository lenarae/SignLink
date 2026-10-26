// ============================================================
// File: settings.gradle.kts
// Purpose: Tells Gradle where to find plugins and dependencies,
// and which modules are part of this project.
// ============================================================

pluginManagement {
    repositories {
        // Google's Maven repository (Android plugins live here)
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central (most Kotlin/Java libraries)
        mavenCentral()
        // Gradle Plugin Portal (community plugins)
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// The project name (shown in Android Studio)
rootProject.name = "SignLink"

// Include the app module (the only module we have)
include(":app")