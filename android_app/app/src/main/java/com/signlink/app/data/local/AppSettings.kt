// ============================================================
// File: data/local/AppSettings.kt
// Purpose: Data class holding all user-configurable settings.
//
// WHY DataStore instead of SharedPreferences?
//   DataStore is the modern replacement. It:
//     - Stores data asynchronously (no ANR risk)
//     - Is coroutine/Flow-based (fits our architecture)
//     - Is type-safe with Preferences DataStore
//     - Handles concurrent reads/writes safely
//
// HOW IT WORKS:
//   AppSettingsDataStore reads/writes key-value pairs from
//   a file called "signlink_settings.preferences_pb" on disk.
//   Each setting has a typed Preferences.Key.
//
// This file defines:
//   1. AppSettings data class (the in-memory model)
//   2. Key constants for DataStore
//   3. Default values
// ============================================================

package com.signlink.app.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.signlink.app.data.repository.RetentionPolicy

/**
 * Snapshot of all user preferences at a point in time.
 * Emitted by AppSettingsDataStore as a StateFlow.
 *
 * @param theme             Selected visual theme (Light, Dark, High Contrast, System)
 * @param textSizeScale     1.0 = normal, 1.25 = large, 1.5 = extra-large
 * @param vibrationEnabled  true = haptic feedback on gesture detected
 * @param ttsEnabled        true = auto-speak translations
 * @param ttsRate           TTS speed 0.5–2.0 (1.0 = normal)
 * @param ttsPitch          TTS pitch 0.5–2.0 (1.0 = normal)
 * @param retentionPolicy   How long to keep chat history
 * @param storageEnabled    false = don't save any messages to DB
 * @param autoConnect       true = auto-reconnect last known device
 * @param showConfidence    true = show confidence % on translations
 * @param appVersion        Read-only display in About section
 */
data class AppSettings(
    val theme:            ThemeMode       = ThemeMode.SYSTEM,
    val textSizeScale:    Float           = 1.0f,
    val vibrationEnabled: Boolean         = true,
    val ttsEnabled:       Boolean         = true,
    val ttsRate:          Float           = 1.0f,
    val ttsPitch:         Float           = 1.0f,
    val retentionPolicy:  RetentionPolicy = RetentionPolicy.FOREVER,
    val storageEnabled:   Boolean         = true,
    val autoConnect:      Boolean         = true,
    val showConfidence:   Boolean         = true,
    val appVersion:       String          = "1.0.0"
)

/** All DataStore preference keys — one per setting */
object SettingsKeys {
    val THEME              = stringPreferencesKey("theme_mode")
    val TEXT_SIZE_SCALE    = floatPreferencesKey("text_size_scale")
    val VIBRATION_ENABLED  = booleanPreferencesKey("vibration_enabled")
    val TTS_ENABLED        = booleanPreferencesKey("tts_enabled")
    val TTS_RATE           = floatPreferencesKey("tts_rate")
    val TTS_PITCH          = floatPreferencesKey("tts_pitch")
    val RETENTION_POLICY   = stringPreferencesKey("retention_policy")
    val STORAGE_ENABLED    = booleanPreferencesKey("storage_enabled")
    val AUTO_CONNECT       = booleanPreferencesKey("auto_connect")
    val SHOW_CONFIDENCE    = booleanPreferencesKey("show_confidence")
}

/** Supported visual themes */
enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System"),
    HIGH_CONTRAST("Contrast")
}

/** Human-readable text-size option for the settings UI */
enum class TextSizeOption(val label: String, val scale: Float) {
    SMALL("Small",       0.85f),
    NORMAL("Normal",     1.0f),
    LARGE("Large",       1.25f),
    EXTRA_LARGE("XL",   1.5f);

    companion object {
        fun fromScale(scale: Float) = values().minByOrNull {
            kotlin.math.abs(it.scale - scale)
        } ?: NORMAL
    }
}