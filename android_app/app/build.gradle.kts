import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// ============================================================
// File: app/build.gradle.kts (App-level)
// Purpose: Configures THIS specific app module.
// Declares all the libraries (dependencies) SignLink needs.
// ============================================================

plugins {
    // Apply the Android app plugin
    alias(libs.plugins.android.application)

    // Apply Kotlin for Android
    alias(libs.plugins.kotlin.android)

    // Apply Compose compiler plugin
    alias(libs.plugins.kotlin.compose)

    // KSP needed for Room to auto-generate database code
    alias(libs.plugins.ksp)

    // Hilt for dependency injection
    alias(libs.plugins.hilt)
}

// Force Gradle to use Java 17 toolchain from a full JDK (includes jlink)
kotlin {
    jvmToolchain(17)
}

configure<ApplicationExtension> {
    // Your app's unique identifier - must match your package
    namespace = "com.signlink.app"

    // Target the latest stable Android SDK
    compileSdk = 35

    defaultConfig {
        applicationId = "com.signlink.app"

        // Minimum Android version: Android 8.0 (needed for BLE features)
        minSdk = 26

        // We target Android 15
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Minification shrinks APK size for production
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Use Java 17 features (modern, stable)
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // Enable Jetpack Compose
        compose = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Force all configurations (including KSP/Hilt) to use metadata-jvm 0.9.0
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-metadata-jvm") {
            useVersion("0.9.0")
        }
    }
}

dependencies {
    // ── Core Android ──────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ── Compose BOM (Bill of Materials) ───────────────────────
    // The BOM ensures ALL compose libraries use compatible versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Extended Material Icons (we'll use many icons throughout the app)
    implementation(libs.androidx.material.icons.extended)

    // Material Components (XML) - needed for themes.xml to work
    implementation(libs.google.material)

    // ── Navigation ────────────────────────────────────────────
    // Handles moving between screens in Compose
    implementation(libs.androidx.navigation.compose)

    // ── ViewModel ─────────────────────────────────────────────
    // ViewModel survives screen rotations & config changes
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ── Room Database ─────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // KSP generates Room code at compile time (no reflection needed)
    ksp(libs.androidx.room.compiler)

    // ── Hilt ──────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // ── DataStore ─────────────────────────────────────────────
    // Better replacement for SharedPreferences (for Settings)
    implementation(libs.androidx.datastore.preferences)

    // ── Coroutines ────────────────────────────────────────────
    // For async work: BLE scanning, DB queries, etc.
    implementation(libs.kotlinx.coroutines.android)

    // ── Testing ───────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Fix for "task 'unitTestClasses' not found in project ':app'"
tasks.register("unitTestClasses") {
    group = "verification"
    description = "Assembles all unit test classes."
    dependsOn(tasks.matching { it.name.endsWith("UnitTestClasses") })
}
