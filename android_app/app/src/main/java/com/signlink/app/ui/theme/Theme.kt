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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.signlink.app.data.local.ThemeMode

// ── Custom Theme Extensions ──────────────────────────────────
data class SignLinkColors(
    val success: Color,
    val error: Color
)

val LocalSignLinkColors = staticCompositionLocalOf {
    SignLinkColors(
        success = Success,
        error = Error
    )
}

object SignLinkTheme {
    val colors: SignLinkColors
        @Composable
        get() = LocalSignLinkColors.current
}

// ── Light Color Scheme ────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008399),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFADEAFF),
    onPrimaryContainer = Color(0xFF001F26),
    
    secondary = Color(0xFF006173),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFADEAFF),
    onSecondaryContainer = Color(0xFF00363F),
    
    tertiary = Color(0xFF6D5677),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6D9FF),
    onTertiaryContainer = Color(0xFF251431),
    
    error = Error,
    onError = Color.White,
    
    background = Color(0xFFD6F6FF),
    onBackground = Color(0xFF001F26),
    surface = Color.White,
    onSurface = Color(0xFF001F26),
    surfaceVariant = Color(0xFFADEAFF),
    onSurfaceVariant = Color(0xFF004F5C),
    outline = Color(0xFF00697A)
)

// ── Dark Color Scheme ─────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF001F26),
    primaryContainer = Color(0xFF004F5C),
    onPrimaryContainer = Color(0xFFADEAFF),
    
    secondary = Color(0xFF55D6F3),
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF00363F),
    onSecondaryContainer = Color(0xFFADEAFF),
    
    tertiary = Color(0xFFD9BDE3),
    onTertiary = Color(0xFF251431),
    tertiaryContainer = Color(0xFF6D5677),
    onTertiaryContainer = Color(0xFFF6D9FF),
    
    error = Color(0xFFE57373),
    onError = Color(0xFF310001),
    
    background = Color(0xFF001D26),
    onBackground = Color(0xFFADEAFF),
    surface = Color(0xFF001F26),
    onSurface = Color(0xFFADEAFF),
    surfaceVariant = Color(0xFF00363F),
    onSurfaceVariant = Color(0xFFADEAFF),
    outline = Color(0xFF00697A)
)

// ── High Contrast Color Scheme ────────────────────────────────
private val HighContrastColorScheme = darkColorScheme(
    primary = HighContrastAccent,
    onPrimary = HighContrastBackground,
    primaryContainer = HighContrastSurface,
    onPrimaryContainer = HighContrastText,
    background = HighContrastBackground,
    onBackground = HighContrastText,
    surface = HighContrastSurface,
    onSurface = HighContrastText,
    outline = HighContrastAccent,
    error = Color.Red,
    onError = Color.Black
)

@Composable
fun SignLinkTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    textSizeScale: Float = 1.0f,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.HIGH_CONTRAST -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        themeMode == ThemeMode.HIGH_CONTRAST -> HighContrastColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Apply scaling to typography
    val scaledTypography = remember(textSizeScale) {
        SignLinkTypography.copy(
            displayLarge = SignLinkTypography.displayLarge.copy(
                fontSize = SignLinkTypography.displayLarge.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.displayLarge.lineHeight * textSizeScale
            ),
            displayMedium = SignLinkTypography.displayMedium.copy(
                fontSize = SignLinkTypography.displayMedium.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.displayMedium.lineHeight * textSizeScale
            ),
            displaySmall = SignLinkTypography.displaySmall.copy(
                fontSize = SignLinkTypography.displaySmall.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.displaySmall.lineHeight * textSizeScale
            ),
            headlineLarge = SignLinkTypography.headlineLarge.copy(
                fontSize = SignLinkTypography.headlineLarge.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.headlineLarge.lineHeight * textSizeScale
            ),
            headlineMedium = SignLinkTypography.headlineMedium.copy(
                fontSize = SignLinkTypography.headlineMedium.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.headlineMedium.lineHeight * textSizeScale
            ),
            headlineSmall = SignLinkTypography.headlineSmall.copy(
                fontSize = SignLinkTypography.headlineSmall.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.headlineSmall.lineHeight * textSizeScale
            ),
            titleLarge = SignLinkTypography.titleLarge.copy(
                fontSize = SignLinkTypography.titleLarge.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.titleLarge.lineHeight * textSizeScale
            ),
            titleMedium = SignLinkTypography.titleMedium.copy(
                fontSize = SignLinkTypography.titleMedium.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.titleMedium.lineHeight * textSizeScale
            ),
            titleSmall = SignLinkTypography.titleSmall.copy(
                fontSize = SignLinkTypography.titleSmall.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.titleSmall.lineHeight * textSizeScale
            ),
            bodyLarge = SignLinkTypography.bodyLarge.copy(
                fontSize = SignLinkTypography.bodyLarge.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.bodyLarge.lineHeight * textSizeScale
            ),
            bodyMedium = SignLinkTypography.bodyMedium.copy(
                fontSize = SignLinkTypography.bodyMedium.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.bodyMedium.lineHeight * textSizeScale
            ),
            bodySmall = SignLinkTypography.bodySmall.copy(
                fontSize = SignLinkTypography.bodySmall.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.bodySmall.lineHeight * textSizeScale
            ),
            labelLarge = SignLinkTypography.labelLarge.copy(
                fontSize = SignLinkTypography.labelLarge.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.labelLarge.lineHeight * textSizeScale
            ),
            labelMedium = SignLinkTypography.labelMedium.copy(
                fontSize = SignLinkTypography.labelMedium.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.labelMedium.lineHeight * textSizeScale
            ),
            labelSmall = SignLinkTypography.labelSmall.copy(
                fontSize = SignLinkTypography.labelSmall.fontSize * textSizeScale,
                lineHeight = SignLinkTypography.labelSmall.lineHeight * textSizeScale
            )
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = {
            val customColors = when {
                themeMode == ThemeMode.HIGH_CONTRAST -> SignLinkColors(SuccessHC, ErrorHC)
                darkTheme -> SignLinkColors(SuccessDark, ErrorDark)
                else -> SignLinkColors(Success, Error)
            }
            CompositionLocalProvider(LocalSignLinkColors provides customColors) {
                content()
            }
        }
    )
}
