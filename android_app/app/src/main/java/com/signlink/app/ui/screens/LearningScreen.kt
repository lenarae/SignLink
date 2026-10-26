package com.signlink.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun LearningScreen(navController: NavHostController) {
    PlaceholderScreen(
        screenName = "Learning Mode",
        icon = "🎓",
        description = "Interactive lessons to help you learn and practice sign language."
    )
}
