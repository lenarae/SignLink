package com.signlink.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signlink.app.data.repository.ChatRepository
import com.signlink.app.data.translation.TranslationEvent
import com.signlink.app.data.translation.TranslationRepository
import com.signlink.app.data.translation.TranslationStatus
import com.signlink.app.utils.TextToSpeechManager
import com.signlink.app.utils.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val translationRepository: TranslationRepository,
    private val ttsManager:            TextToSpeechManager,
    private val chatRepository:        ChatRepository
) : ViewModel() {

    val translationStatus: StateFlow<TranslationStatus> = translationRepository.status
    val isPaused:          StateFlow<Boolean>           = translationRepository.isPaused
    val sessionText:       StateFlow<String>            = translationRepository.sessionText
    val ttsState:          StateFlow<TtsState>          = ttsManager.ttsState

    private val _eventHistory = MutableStateFlow<List<TranslationEvent>>(emptyList())
    val eventHistory: StateFlow<List<TranslationEvent>> = _eventHistory.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _latestWord = MutableStateFlow("")
    val latestWord: StateFlow<String> = _latestWord.asStateFlow()

    init {
        viewModelScope.launch {
            translationRepository.events.collect { event ->
                _eventHistory.value = listOf(event) + _eventHistory.value.take(49)
                _latestWord.value   = event.text

                if (_ttsEnabled.value) ttsManager.speak(event.text)

                // Auto-save to Room DB
                chatRepository.saveTranslation(
                    text       = event.text,
                    confidence = event.confidence
                )
            }
        }
    }

    fun togglePause()       = translationRepository.togglePause()
    fun toggleTts()         { _ttsEnabled.value = !_ttsEnabled.value; if (!_ttsEnabled.value) ttsManager.stop() }
    fun speakSessionText()  { val t = sessionText.value; if (t.isNotBlank()) ttsManager.speak(t, true) }
    fun speakWord(w: String) = ttsManager.speak(w, true)
    fun setTtsRate(r: Float) = ttsManager.setRate(r)
    fun clearSession()      {
        translationRepository.clearSession()
        _eventHistory.value = emptyList()
        _latestWord.value   = ""
        ttsManager.stop()
    }

    override fun onCleared() { super.onCleared(); ttsManager.stop() }
}