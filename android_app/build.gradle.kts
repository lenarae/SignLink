// ============================================================
// File: build.gradle.kts (Project-level)
// Purpose: Defines plugins available to all modules.
// Think of this as the "master settings" for the whole project.
// ============================================================

plugins {
    // Android application plugin - needed to build an Android app
    alias(libs.plugins.android.application) apply false

    // Kotlin plugin - we're writing everything in Kotlin
    alias(libs.plugins.kotlin.android) apply false

    // Kotlin Compose compiler plugin - enables Jetpack Compose
    alias(libs.plugins.kotlin.compose) apply false

    // KSP (Kotlin Symbol Processing) - needed by RoomDB to generate code
    alias(libs.plugins.ksp) apply false

    // Hilt - Dependency injection (we'll use it lightly, mainly for ViewModel)
    alias(libs.plugins.hilt) apply false
}