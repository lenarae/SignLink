package com.signlink.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signlink.app.data.repository.ChatRepository
import com.signlink.app.utils.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextToSpeechViewModel @Inject constructor(
    private val ttsManager: TextToSpeechManager,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val ttsState = ttsManager.ttsState

    private val _textInput = MutableStateFlow("")
    val textInput: StateFlow<String> = _textInput.asStateFlow()

    fun onTextChanged(newText: String) {
        _textInput.value = newText
    }

    fun speak() {
        val text = _textInput.value
        if (text.isNotBlank()) {
            ttsManager.speak(text, flushQueue = true)
            
            // Auto-save to Room DB
            viewModelScope.launch {
                chatRepository.saveTtsResult(text)
            }
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun clearText() {
        _textInput.value = ""
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}
