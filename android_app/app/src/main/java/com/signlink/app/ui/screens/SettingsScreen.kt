package com.signlink.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun SettingsScreen(navController: NavHostController) {
    PlaceholderScreen(
        screenName = "Settings",
        icon = "⚙️",
        description = "Configure app preferences, accessibility options, and device settings."
    )
}
