
package com.signlink.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.signlink.app.ui.screens.WelcomeScreen
import com.signlink.app.ui.screens.UserTypeScreen
import com.signlink.app.ui.screens.PairDeviceScreen
import com.signlink.app.ui.screens.HomeScreen
import com.signlink.app.ui.screens.BluetoothScreen
import com.signlink.app.ui.screens.CalibrationScreen
import com.signlink.app.ui.screens.TranslationScreen
import com.signlink.app.ui.screens.SpeechScreen
import com.signlink.app.ui.screens.TextToSpeechScreen
import com.signlink.app.ui.screens.ChatHistoryScreen
import com.signlink.app.ui.screens.SettingsScreen
import com.signlink.app.ui.screens.LearningScreen

@Composable
fun SignLinkNavGraph(
    navController:    NavHostController,
    startDestination: String = Screen.Welcome.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ── Onboarding ───────────────────────────────
        composable(Screen.Welcome.route)  { WelcomeScreen(navController) }
        composable(Screen.UserType.route) { UserTypeScreen(navController) }
        composable(Screen.PairDevice.route) { PairDeviceScreen(navController) }

        // ── Home ─────────────────────────────────────
        composable(Screen.Home.route) { HomeScreen(navController) }

        // ── Bluetooth ────────────────────────────────
        composable(Screen.Bluetooth.route) { BluetoothScreen(navController) }

        // ── Calibration ──────────────────────────────
        composable(Screen.Calibration.route) { CalibrationScreen(navController) }

        // ── Translation ──────────────────────────────
        composable(Screen.Translation.route) { TranslationScreen(navController) }

        // ── Speech ───────────────────────────────────
        composable(Screen.Speech.route) { SpeechScreen(navController) }
        composable(Screen.TextToSpeech.route) { TextToSpeechScreen(navController) }

        // ── Chat History ─────────────────────────────
        composable(Screen.ChatHistory.route) { ChatHistoryScreen(navController) }

        // ── Settings ─────────────────────────────────
        composable(Screen.Settings.route) { SettingsScreen(navController) }

        // ── Learning ─────────────────────────────────
        composable(Screen.Learning.route) { LearningScreen(navController) }
    }
}