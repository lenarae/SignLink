package com.signlink.app.viewmodel

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signlink.app.data.local.AppSettings
import com.signlink.app.data.local.AppSettingsDataStore
import com.signlink.app.data.local.TextSizeOption
import com.signlink.app.data.local.ThemeMode
import com.signlink.app.data.local.UserType
import com.signlink.app.data.repository.ChatRepository
import com.signlink.app.data.repository.RetentionPolicy
import com.signlink.app.utils.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: AppSettingsDataStore,
    private val chatRepository:    ChatRepository,
    private val ttsManager:        TextToSpeechManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ── Current settings (live from DataStore) ────────────────
    val settings: StateFlow<AppSettings> = settingsDataStore.settings
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = AppSettings()
        )

    // ── Reset confirmation dialog state ───────────────────────
    private val _showResetDialog      = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    private val _showClearDataDialog  = MutableStateFlow(false)
    val showClearDataDialog: StateFlow<Boolean> = _showClearDataDialog.asStateFlow()

    // ── Snackbar / feedback state ─────────────────────────────
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()


    // APPEARANCE
    fun setTheme(mode: ThemeMode) {
        update { setTheme(mode) }
    }

    fun setUserType(type: UserType) {
        update { setUserType(type) }
    }

    fun completeOnboarding() {
        update { setOnboardingCompleted(true) }
    }

    fun setTextSize(option: TextSizeOption) = update { setTextSizeScale(option.scale) }


    // ACCESSIBILITY
    fun setVibration(enabled: Boolean) {
        update { setVibrationEnabled(enabled) }
        if (enabled) triggerTestVibration()
    }


    // TEXT-TO-SPEECH
    fun setTtsEnabled(enabled: Boolean) = update { setTtsEnabled(enabled) }

    fun setTtsRate(rate: Float) {
        update { setTtsRate(rate) }
        ttsManager.setRate(rate)
    }

    fun setTtsPitch(pitch: Float) {
        update { setTtsPitch(pitch) }
        ttsManager.setPitch(pitch)
        // Play a test word so user hears the new pitch immediately
        ttsManager.speak("Hello", flushQueue = true)
    }


    // DATA & STORAGE
    fun setStorageEnabled(enabled: Boolean) = update { setStorageEnabled(enabled) }

    fun setRetentionPolicy(policy: RetentionPolicy) {
        update { setRetentionPolicy(policy) }
        // Apply the policy immediately to existing data
        viewModelScope.launch {
            chatRepository.applyRetentionPolicy(policy)
            _feedbackMessage.value = "Retention policy applied"
        }
    }

    fun showClearDataDialog()  { _showClearDataDialog.value = true }
    fun hideClearDataDialog()  { _showClearDataDialog.value = false }

    fun clearAllChatData() {
        viewModelScope.launch {
            chatRepository.deleteAllMessages()
            hideClearDataDialog()
            _feedbackMessage.value = "All chat history cleared"
        }
    }

    // DEVICE
    fun setAutoConnect(enabled: Boolean) = update { setAutoConnect(enabled) }

    fun setShowConfidence(show: Boolean) = update { setShowConfidence(show) }


    // RESET
    fun showResetDialog()  { _showResetDialog.value = true }
    fun hideResetDialog()  { _showResetDialog.value = false }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsDataStore.resetToDefaults()
            ttsManager.setRate(1.0f)
            ttsManager.setPitch(1.0f)
            hideResetDialog()
            _feedbackMessage.value = "Settings reset to defaults"
        }
    }

    fun clearFeedback() { _feedbackMessage.value = null }


    // PRIVATE HELPERS
    /** Launches a coroutine to call a DataStore write function */
    private fun update(block: suspend AppSettingsDataStore.() -> Unit) {
        viewModelScope.launch { settingsDataStore.block() }
    }

    /** Fire a short haptic pulse so user feels the vibration toggle */
    private fun triggerTestVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                        as VibratorManager
                vm.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(80)
                }
            }
        } catch (_: Exception) {
            // Silently ignore — vibration is non-critical
        }
    }
}