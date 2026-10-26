// ============================================================
// File: navigation/NavGraph.kt  [FINAL — Phase 8 + 9]
// ALL screens wired. No placeholders remain.
// ============================================================

package com.signlink.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.signlink.app.ui.screens.WelcomeScreen
import com.signlink.app.ui.screens.LoginScreen
import com.signlink.app.ui.screens.RegisterScreen
import com.signlink.app.ui.screens.HomeScreen
import com.signlink.app.ui.screens.BluetoothScreen
import com.signlink.app.ui.screens.CalibrationScreen
import com.signlink.app.ui.screens.TranslationScreen
import com.signlink.app.ui.screens.SpeechScreen
import com.signlink.app.ui.screens.ChatHistoryScreen
import com.signlink.app.ui.screens.SettingsScreen
import com.signlink.app.ui.screens.LearningScreen

@Composable
fun SignLinkNavGraph(
    navController:    NavHostController,
    startDestination: String = Screen.Welcome.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ── Phase 2: Onboarding ───────────────────────────────
        composable(Screen.Welcome.route)  { WelcomeScreen(navController) }
        composable(Screen.Login.route)    { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }

        // ── Phase 1: Home ─────────────────────────────────────
        composable(Screen.Home.route) { HomeScreen(navController) }

        // ── Phase 3: Bluetooth ────────────────────────────────
        composable(Screen.Bluetooth.route) { BluetoothScreen(navController) }

        // ── Phase 4: Calibration ──────────────────────────────
        composable(Screen.Calibration.route) { CalibrationScreen(navController) }

        // ── Phase 5: Translation ──────────────────────────────
        composable(Screen.Translation.route) { TranslationScreen(navController) }

        // ── Phase 6: Speech ───────────────────────────────────
        composable(Screen.Speech.route) { SpeechScreen(navController) }

        // ── Phase 7: Chat History ─────────────────────────────
        composable(Screen.ChatHistory.route) { ChatHistoryScreen(navController) }

        // ── Phase 8: Settings ─────────────────────────────────
        composable(Screen.Settings.route) { SettingsScreen(navController) }

        // ── Phase 9: Learning ─────────────────────────────────
        composable(Screen.Learning.route) { LearningScreen(navController) }
    }
}