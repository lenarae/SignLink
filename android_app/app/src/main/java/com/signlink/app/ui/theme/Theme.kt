// ============================================================
// File: ui/theme/Theme.kt
// Purpose: The master theme for SignLink.
//
// This file defines:
//   1. Light color scheme (default)
//   2. Dark color scheme
//   3. High contrast mode (for accessibility)
//   4. SignLinkTheme composable that wraps the whole app
//
// Every screen inherits colors/typography from here automatically.
// ============================================================

package com.signlink.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Color Scheme ────────────────────────────────────────
// Used when the user's device is in Light Mode
private val LightColorScheme = lightColorScheme(
    // Primary = main interactive color (buttons, FAB, etc.)
    primary          = SignLinkTeal500,
    onPrimary        = SignLinkWhite,
    primaryContainer = SignLinkTeal100,
    onPrimaryContainer = SignLinkTeal900,

    // Secondary = supporting color
    secondary          = SignLinkTeal700,
    onSecondary        = SignLinkWhite,
    secondaryContainer = SignLinkTeal50,
    onSecondaryContainer = SignLinkTeal800,

    // Tertiary = accent/highlight (status indicators)
    tertiary          = SignLinkCyan,
    onTertiary        = SignLinkNeutral900,
    tertiaryContainer = SignLinkTeal100,
    onTertiaryContainer = SignLinkTeal900,

    // Background = screen background
    background = SignLinkNeutral50,
    onBackground = SignLinkNeutral900,

    // Surface = cards, sheets, dialogs
    surface = SignLinkWhite,
    onSurface = SignLinkNeutral900,
    surfaceVariant = SignLinkNeutral100,
    onSurfaceVariant = SignLinkNeutral600,

    // Error colors
    error = SignLinkDisconnected,
    onError = SignLinkWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Outlines (borders)
    outline = SignLinkNeutral300,
    outlineVariant = SignLinkNeutral200
)

// ── Dark Color Scheme ─────────────────────────────────────────
// Used when the user's device is in Dark Mode
private val DarkColorScheme = darkColorScheme(
    primary          = SignLinkTeal300,
    onPrimary        = SignLinkTeal900,
    primaryContainer = SignLinkTeal700,
    onPrimaryContainer = SignLinkTeal100,

    secondary          = SignLinkTeal200,
    onSecondary        = SignLinkTeal800,
    secondaryContainer = SignLinkTeal800,
    onSecondaryContainer = SignLinkTeal100,

    tertiary          = SignLinkCyan,
    onTertiary        = SignLinkNeutral900,
    tertiaryContainer = SignLinkTeal700,
    onTertiaryContainer = SignLinkTeal100,

    background = SignLinkNeutral950,
    onBackground = SignLinkNeutral100,

    surface = SignLinkNeutral900,
    onSurface = SignLinkNeutral100,
    surfaceVariant = SignLinkNeutral800,
    onSurfaceVariant = SignLinkNeutral400,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = SignLinkNeutral600,
    outlineVariant = SignLinkNeutral700
)

// ── High Contrast Color Scheme ────────────────────────────────
// For users with visual impairments (Phase 8 Settings toggle)
private val HighContrastColorScheme = darkColorScheme(
    primary          = HighContrastAccent,
    onPrimary        = HighContrastBackground,
    primaryContainer = HighContrastSurface,
    onPrimaryContainer = HighContrastText,

    background = HighContrastBackground,
    onBackground = HighContrastText,

    surface = HighContrastSurface,
    onSurface = HighContrastText,
    surfaceVariant = HighContrastSurface,
    onSurfaceVariant = HighContrastText,

    outline = HighContrastAccent
)

// ── SignLinkTheme Composable ──────────────────────────────────
/**
 * The main theme wrapper for the entire SignLink app.
 *
 * How to use:
 *   SignLinkTheme {
 *       // Your screens go here
 *   }
 *
 * @param darkTheme - true = use dark colors (defaults to system setting)
 * @param highContrast - true = use high contrast mode (from Settings)
 * @param dynamicColor - true = use Android 12+ wallpaper-based colors
 * @param content - the composable content to theme
 */
@Composable
fun SignLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    dynamicColor: Boolean = false,  // Disabled by default for brand consistency
    content: @Composable () -> Unit
) {
    // Choose the appropriate color scheme
    val colorScheme = when {
        // High contrast overrides everything (accessibility first)
        highContrast -> HighContrastColorScheme

        // Android 12+ supports dynamic color from wallpaper
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        // Our custom dark theme
        darkTheme -> DarkColorScheme

        // Our custom light theme (default)
        else -> LightColorScheme
    }

    // Update the status bar color to match our theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar transparent so our app color shows through
            window.statusBarColor = colorScheme.background.toArgb()
            // Tell Android whether to use light or dark status bar icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Apply the Material 3 theme with our custom schemes
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SignLinkTypography,
        content = content
    )
}

// ── Helper: import Color for high contrast scheme ─────────────
// We need this because Color is normally in Color.kt
private fun Color(value: Long) = androidx.compose.ui.graphics.Color(value.toULong())