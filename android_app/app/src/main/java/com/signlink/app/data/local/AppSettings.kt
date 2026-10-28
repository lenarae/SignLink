package com.signlink.app.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.signlink.app.data.repository.RetentionPolicy


enum class UserType(val label: String) {
    DEAF("Deaf"),
    MUTE("Mute"),
    HEARING("Hearing"),
    LEARNER("Learner"),
    UNDECIDED("Not Set")
}

/**
 * Snapshot of all user preferences at a point in time.
 * Emitted by AppSettingsDataStore as a StateFlow.
 *
 * @param theme
 * @param textSizeScale
 * @param userType
 * @param vibrationEnabled
 * @param ttsEnabled
 * @param ttsRate
 * @param ttsPitch
 * @param retentionPolicy
 * @param storageEnabled
 * @param autoConnect
 * @param showConfidence
 * @param appVersion
 */
data class AppSettings(
    val theme:            ThemeMode       = ThemeMode.SYSTEM,
    val textSizeScale:    Float           = 1.0f, // Small is baseline
    val userType:         UserType        = UserType.UNDECIDED,
    val onboardingCompleted: Boolean      = false,
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
    val USER_TYPE          = stringPreferencesKey("user_type")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val VIBRATION_ENABLED  = booleanPreferencesKey("vibration_enabled")
    val TTS_ENABLED        = booleanPreferencesKey("tts_enabled")
    val TTS_RATE           = floatPreferencesKey("tts_rate")
    val TTS_PITCH          = floatPreferencesKey("tts_pitch")
    val RETENTION_POLICY   = stringPreferencesKey("retention_policy")
    val STORAGE_ENABLED    = booleanPreferencesKey("storage_enabled")
    val AUTO_CONNECT       = booleanPreferencesKey("auto_connect")
    val SHOW_CONFIDENCE    = booleanPreferencesKey("show_confidence")
}


enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System"),
    HIGH_CONTRAST("Contrast")
}


enum class TextSizeOption(val label: String, val scale: Float) {
    SMALL("Small",       1.0f),
    MEDIUM("Medium",     1.07f),
    LARGE("Large",       1.15f);

    companion object {
        fun fromScale(scale: Float) = entries.minByOrNull {
            kotlin.math.abs(it.scale - scale)
        } ?: SMALL
    }
}