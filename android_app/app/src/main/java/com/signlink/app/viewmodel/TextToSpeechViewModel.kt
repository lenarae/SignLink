package com.signlink.app.viewmodel

import androidx.lifecycle.ViewModel
import com.signlink.app.utils.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TextToSpeechViewModel @Inject constructor(
    private val ttsManager: TextToSpeechManager
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
