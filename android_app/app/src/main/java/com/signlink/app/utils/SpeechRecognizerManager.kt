package com.signlink.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale
import javax.inject.Inject

// ── Speech State sealed class ─────────────────────────────────

sealed class SpeechState {
    /** Mic is off.  */
    data object Idle : SpeechState()

    /** Mic is active, waiting for speech input. */
    data object Listening : SpeechState()

    /**
     * Partial transcript received in real-time.
     * @param text The partial transcription so far
     */
    data class Partial(val text: String) : SpeechState()

    /**
     * Final recognition result ready.
     * @param text The fully-recognised transcript
     */
    data class Result(val text: String) : SpeechState()

    /** Listened but heard nothing. Not an error. */
    data object NoSpeech : SpeechState()

    /**
     * A real error occurred (mic hardware, network, permission...).
     * @param message Human-readable message to show the user
     * @param code    Android's original error code for debugging
     */
    data class Error(val message: String, val code: Int = -1) : SpeechState()
}

/**
 * Manages Android SpeechRecognizer with a clean StateFlow API.
 *
 * LIFECYCLE: NOT @Singleton. Tied to ViewModel lifetime.
 *            Call destroy() from ViewModel.onCleared().
 *
 * USAGE:
 *   class MyViewModel @Inject constructor(
 *       private val speechManager: SpeechRecognizerManager
 *   )
 */
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // All SpeechRecognizer calls MUST be on Main thread
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    /**
     * Whether Google STT service is installed on this device.
     * Does NOT check microphone permission — handle that in the UI.
     */
    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    private var recognizer: SpeechRecognizer? = null
    private var mockJob: Job? = null

    private val mockPhrases = listOf(
        "Hello, how are you today?",
        "I need some help please",
        "Thank you very much",
        "Can you understand me?",
        "SignLink is working great",
        "Nice to meet you",
        "Please speak slowly",
        "I am using SignLink"
    )
    private var mockIndex = 0


    // PUBLIC API

    fun startListening() {
        val current = _speechState.value
        if (current is SpeechState.Listening || current is SpeechState.Partial) return

        if (isAvailable) startRealRecognition()
        else                        startMockRecognition()
    }


    fun stopListening() {
        recognizer?.stopListening()
        mockJob?.cancel()
        _speechState.value = SpeechState.Idle
    }


    fun destroy() {
        mockJob?.cancel()
        mainScope.cancel()
        recognizer?.destroy()
        recognizer = null
        _speechState.value = SpeechState.Idle
    }


    // REAL RECOGNITION
    private fun startRealRecognition() {
        mainScope.launch {
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                // FREE_FORM = recognise natural speech, not search queries
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // Use device locale so "en-GB" users get British English, etc.
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                // CRITICAL: enables word-by-word real-time updates
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Wait up to 5s for user to start speaking
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                // Stop 2s after user stops speaking
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            }

            recognizer?.setRecognitionListener(buildListener())
            recognizer?.startListening(intent)
        }
    }


    private fun buildListener() = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            _speechState.value = SpeechState.Listening
        }

        override fun onBeginningOfSpeech() {

        }

        override fun onRmsChanged(rmsdB: Float) {

        }

        override fun onBufferReceived(buffer: ByteArray?) {

        }

        override fun onEndOfSpeech() {

        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?: return

            if (partial.isNotBlank()) _speechState.value = SpeechState.Partial(partial)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?: ""

            _speechState.value = if (text.isNotBlank())
                SpeechState.Result(text)
            else
                SpeechState.NoSpeech
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH ->
                    "Could not understand. Please try again."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                    "No speech detected. Tap the mic and speak clearly."
                SpeechRecognizer.ERROR_AUDIO ->
                    "Microphone error. Check that no other app is using it."
                SpeechRecognizer.ERROR_NETWORK ->
                    "Network error. Speech recognition requires internet."
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                    "Network timed out. Check your connection."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                    "Recogniser is busy. Wait a moment and try again."
                SpeechRecognizer.ERROR_SERVER ->
                    "Server error. Try again in a moment."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                    "Microphone permission is required."
                SpeechRecognizer.ERROR_TOO_MANY_REQUESTS ->
                    "Too many requests. Please wait a few seconds."
                else ->
                    "Recognition failed (code $error). Please try again."
            }


            _speechState.value = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechState.NoSpeech
                else                                  -> SpeechState.Error(message, error)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {

        }
    }


    private fun startMockRecognition() {
        _speechState.value = SpeechState.Listening
        mockJob?.cancel()

        mockJob = mainScope.launch {
            delay(1_200)

            val phrase = mockPhrases[mockIndex % mockPhrases.size]
            mockIndex++

            // Emit each word progressively to simulate partial results
            val words = phrase.split(" ")
            val sb = StringBuilder()
            for (word in words) {
                if (!isActive) return@launch
                sb.append(if (sb.isEmpty()) word else " $word")
                _speechState.value = SpeechState.Partial(sb.toString())
                delay(180)
            }

            delay(350)
            _speechState.value = SpeechState.Result(phrase)
        }
    }
}