// ============================================================
// File: ui/screens/PlaceholderScreen.kt
// Purpose: A temporary screen shown for features not yet built.
//
// This is a KEY practice in phased development:
//   - The app can be compiled and run at ANY phase
//   - Navigation works end-to-end from Day 1
//   - No "TODO" crashes — just friendly placeholders
//
// Each phase replaces one placeholder with a real screen.
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A placeholder composable displayed for screens not yet implemented.
 *
 * Shows:
 *  - A large emoji icon representing the feature
 *  - The screen name
 *  - A description of what's coming
 *  - A "Work in Progress" badge
 *
 * @param screenName The name of the future screen
 * @param icon An emoji representing the feature (e.g. "🤟" for translation)
 * @param description What this screen will do when built
 */
@Composable
fun PlaceholderScreen(
    screenName: String,
    icon: String,
    description: String
) {
    // Center everything on screen
    Box(
        modifier = Modifier
            .fillMaxSize()                          // Take full screen
            .padding(32.dp),                        // Comfortable padding
        contentAlignment = Alignment.Center         // Center content
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)  // Space between items
        ) {

            // ── Large Icon ────────────────────────────────────
            Text(
                text = icon,
                fontSize = 72.sp                    // Big, friendly icon
            )

            // ── Screen Name ───────────────────────────────────
            Text(
                text = screenName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // ── Description ───────────────────────────────────
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── WIP Badge ─────────────────────────────────────
            // Surface card with a "🚧 Coming Soon" message
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "🚧  Under Construction",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}