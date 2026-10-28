
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
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object UserType : Screen("user_type")
    data object PairDevice : Screen("pair_device")





    // ── Main App Screens ──────────────────────────────────────
    data object Home : Screen("home")
    data object Bluetooth : Screen("bluetooth")
    data object Calibration : Screen("calibration")
    data object Translation : Screen("translation")
    data object Speech : Screen("speech")
    data object TextToSpeech : Screen("tts")
    data object ChatHistory : Screen("chat_history")
    data object Settings : Screen("settings")
    data object Learning : Screen("learning")
}