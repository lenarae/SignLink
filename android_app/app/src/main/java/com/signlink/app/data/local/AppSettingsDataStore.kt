package com.signlink.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ChatRetentionPolicy {
    FOREVER,    // Never auto-delete
    ONE_DAY,    // Delete messages older than 24 hours
    ONE_MONTH,  // Delete messages older than 30 days
    DISABLED    // Don't save any messages
}

data class AppSettings(
    val darkMode: Boolean = false,
    val highContrast: Boolean = false,
    val storageEnabled: Boolean = true,
    val retentionPolicy: ChatRetentionPolicy = ChatRetentionPolicy.FOREVER
)

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
    private val STORAGE_ENABLED = booleanPreferencesKey("storage_enabled")
    private val RETENTION_POLICY = stringPreferencesKey("retention_policy")

    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            darkMode = preferences[DARK_MODE] ?: false,
            highContrast = preferences[HIGH_CONTRAST] ?: false,
            storageEnabled = preferences[STORAGE_ENABLED] ?: true,
            retentionPolicy = try {
                ChatRetentionPolicy.valueOf(preferences[RETENTION_POLICY] ?: ChatRetentionPolicy.FOREVER.name)
            } catch (e: Exception) {
                ChatRetentionPolicy.FOREVER
            }
        )
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun updateHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[HIGH_CONTRAST] = enabled }
    }

    suspend fun updateStorageEnabled(enabled: Boolean) {
        context.dataStore.edit { it[STORAGE_ENABLED] = enabled }
    }

    suspend fun updateRetentionPolicy(policy: ChatRetentionPolicy) {
        context.dataStore.edit { it[RETENTION_POLICY] = policy.name }
    }
}
