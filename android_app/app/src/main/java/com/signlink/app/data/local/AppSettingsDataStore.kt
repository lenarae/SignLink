package com.signlink.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.signlink.app.data.repository.RetentionPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "signlink_settings")

class AppSettingsDataStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs.toAppSettings() }

    // ── Write functions (one per preference) ──────────────────

    suspend fun setTheme(mode: ThemeMode) =
        context.dataStore.edit { it[SettingsKeys.THEME] = mode.name }

    suspend fun setTextSizeScale(scale: Float) =
        context.dataStore.edit { it[SettingsKeys.TEXT_SIZE_SCALE] = scale }

    suspend fun setUserType(type: UserType) =
        context.dataStore.edit { it[SettingsKeys.USER_TYPE] = type.name }

    suspend fun setOnboardingCompleted(completed: Boolean) =
        context.dataStore.edit { it[SettingsKeys.ONBOARDING_COMPLETED] = completed }

    suspend fun setVibrationEnabled(enabled: Boolean) =
        context.dataStore.edit { it[SettingsKeys.VIBRATION_ENABLED] = enabled }

    suspend fun setTtsEnabled(enabled: Boolean) =
        context.dataStore.edit { it[SettingsKeys.TTS_ENABLED] = enabled }

    suspend fun setTtsRate(rate: Float) =
        context.dataStore.edit { it[SettingsKeys.TTS_RATE] = rate }

    suspend fun setTtsPitch(pitch: Float) =
        context.dataStore.edit { it[SettingsKeys.TTS_PITCH] = pitch }

    suspend fun setRetentionPolicy(policy: RetentionPolicy) =
        context.dataStore.edit { it[SettingsKeys.RETENTION_POLICY] = policy.name }

    suspend fun setStorageEnabled(enabled: Boolean) =
        context.dataStore.edit { it[SettingsKeys.STORAGE_ENABLED] = enabled }

    suspend fun setAutoConnect(enabled: Boolean) =
        context.dataStore.edit { it[SettingsKeys.AUTO_CONNECT] = enabled }

    suspend fun setShowConfidence(show: Boolean) =
        context.dataStore.edit { it[SettingsKeys.SHOW_CONFIDENCE] = show }

    suspend fun resetToDefaults() =
        context.dataStore.edit { it.clear() }








    // ── Mapping helper ─────────────────────────────────────────
    private fun Preferences.toAppSettings() = AppSettings(
        theme            = this[SettingsKeys.THEME]
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM,
        textSizeScale    = this[SettingsKeys.TEXT_SIZE_SCALE]   ?: 1.0f,
        userType         = this[SettingsKeys.USER_TYPE]
            ?.let { runCatching { UserType.valueOf(it) }.getOrNull() }
            ?: UserType.UNDECIDED,
        onboardingCompleted = this[SettingsKeys.ONBOARDING_COMPLETED] ?: false,
        vibrationEnabled = this[SettingsKeys.VIBRATION_ENABLED] ?: true,
        ttsEnabled       = this[SettingsKeys.TTS_ENABLED]       ?: true,
        ttsRate          = this[SettingsKeys.TTS_RATE]          ?: 1.0f,
        ttsPitch         = this[SettingsKeys.TTS_PITCH]         ?: 1.0f,
        retentionPolicy  = this[SettingsKeys.RETENTION_POLICY]
            ?.let { runCatching { RetentionPolicy.valueOf(it) }.getOrNull() }
            ?: RetentionPolicy.FOREVER,
        storageEnabled   = this[SettingsKeys.STORAGE_ENABLED]   ?: true,
        autoConnect      = this[SettingsKeys.AUTO_CONNECT]      ?: true,
        showConfidence   = this[SettingsKeys.SHOW_CONFIDENCE]   ?: true
    )
}