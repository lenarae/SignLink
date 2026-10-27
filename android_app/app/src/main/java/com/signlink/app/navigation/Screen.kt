// ============================================================
// File: navigation/Screen.kt
// Purpose: Define ALL screens (routes) in the app.
//
// Using a sealed class means:
//   - Every possible screen is listed in ONE place
//   - The compiler will warn you if you miss a case
//   - No typos in route strings (compile-time safety)
//
// Think of this as the "table of contents" for navigation.
// ============================================================

package com.signlink.app.navigation

/**
 * Sealed class representing every navigable screen in SignLink.
 *
 * Each screen has a [route] string - this is what Navigation
 * Compose uses internally to identify the destination.
 *
 * Sealed class means: no other class outside this file
 * can extend Screen. This keeps our routes controlled.
 */
sealed class Screen(val route: String) {

    // ── Onboarding Flow ───────────────────────────────────────

    /**
     * Welcome/splash screen.
     * First screen shown to new users.
     * Phase 2.
     */
    data object Welcome : Screen("welcome")

    /**
     * Login screen (UI only, no backend).
     * Phase 2.
     */
    data object Login : Screen("login")

    /**
     * Register screen (UI only, no backend).
     * Phase 2.
     */
    data object Register : Screen("register")

    /**
     * User type selection screen (Deaf, Mute, Hearing, Learner).
     * Shown after Get Started.
     */
    data object UserType : Screen("user_type")

    /**
     * Bluetooth pairing screen for onboarding.
     */
    data object PairDevice : Screen("pair_device")

    // ── Main App Screens ──────────────────────────────────────

    /**
     * Home/Dashboard screen.
     * Central hub after login. Shows connection status + quick actions.
     */
    data object Home : Screen("home")

    /**
     * Bluetooth scan + connect screen.
     * Phase 3.
     */
    data object Bluetooth : Screen("bluetooth")

    /**
     * Device calibration wizard.
     * Phase 4. Only accessible after connecting.
     */
    data object Calibration : Screen("calibration")

    /**
     * Real-time gesture translation screen.
     * Phase 5. Core feature of the app.
     */
    data object Translation : Screen("translation")

    /**
     * Speech-to-Text recording screen.
     * Phase 6.
     */
    data object Speech : Screen("speech")

    /**
     * Text-to-Speech input screen.
     * For users to type and have it read aloud.
     */
    data object TextToSpeech : Screen("tts")

    /**
     * Chat history browser.
     * Phase 7. Shows stored conversations from RoomDB.
     */
    data object ChatHistory : Screen("chat_history")

    /**
     * App settings screen.
     * Phase 8. Text size, contrast, vibration, data retention.
     */
    data object Settings : Screen("settings")

    /**
     * Learning mode placeholder.
     * Phase 9. "Coming Soon" UI.
     */
    data object Learning : Screen("learning")
}