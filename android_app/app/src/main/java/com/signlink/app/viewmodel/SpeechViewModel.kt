package com.signlink.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signlink.app.data.repository.ChatRepository
import com.signlink.app.utils.SpeechRecognizerManager
import com.signlink.app.utils.SpeechState
import com.signlink.app.utils.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TranscriptEntry(
    val text:        String,
    val timestampMs: Long = System.currentTimeMillis()
)

@HiltViewModel
class SpeechViewModel @Inject constructor(
    private val speechManager:  SpeechRecognizerManager,
    private val ttsManager:     TextToSpeechManager,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val speechState: StateFlow<SpeechState> = speechManager.speechState
    val isAvailable: Boolean get() = speechManager.isAvailable

    val isListening: StateFlow<Boolean> = speechState
        .map { it is SpeechState.Listening || it is SpeechState.Partial }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val partialText: StateFlow<String> = speechState
        .map { s -> when (s) { is SpeechState.Partial -> s.text; else -> "" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _history = MutableStateFlow<List<TranscriptEntry>>(emptyList())
    val history: StateFlow<List<TranscriptEntry>> = _history.asStateFlow()

    val sessionText: StateFlow<String> = _history
        .map { it.joinToString(" ") { e -> e.text } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val ttsState   = ttsManager.ttsState

    private val _autoSpeak = MutableStateFlow(false)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    init {
        viewModelScope.launch {
            speechState.collect { state ->
                if (state is SpeechState.Result && state.text.isNotBlank()) {
                    _history.value = listOf(TranscriptEntry(state.text)) + _history.value
                    if (_autoSpeak.value) ttsManager.speak(state.text, true)

                    // Auto-save to Room DB
                    chatRepository.saveSpeechResult(state.text)
                }
            }
        }
    }

    fun toggleListening()           { if (isListening.value) speechManager.stopListening() else speechManager.startListening() }
    fun speakEntry(text: String)    = ttsManager.speak(text, true)
    fun speakAll()                  { val t = sessionText.value; if (t.isNotBlank()) ttsManager.speak(t, true) }
    fun stopSpeaking()              = ttsManager.stop()
    fun clearHistory()              { _history.value = emptyList(); ttsManager.stop() }
    fun deleteEntry(e: TranscriptEntry) { _history.value = _history.value.filter { it.timestampMs != e.timestampMs } }
    fun toggleAutoSpeak()           { _autoSpeak.value = !_autoSpeak.value }

    override fun onCleared() { super.onCleared(); speechManager.destroy(); ttsManager.stop() }
}