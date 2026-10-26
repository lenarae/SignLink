// ============================================================
// File: utils/SpeechRecognizerManager.kt
// Purpose: Wraps Android's SpeechRecognizer into a clean,
// coroutine/Flow-based API that the ViewModel can observe.
//
// WHY A WRAPPER?
//   Android's SpeechRecognizer uses old-style callbacks
//   (RecognitionListener). Exposing raw callbacks to ViewModel
//   is messy and lifecycle-unsafe. This wrapper converts those
//   callbacks into StateFlows the ViewModel can collect safely.
//
// HOW ANDROID STT WORKS:
//   1. Create SpeechRecognizer instance (must be on Main thread)
//   2. Create RecognitionListener with ~8 callback methods
//   3. Call recognizer.startListening(intent)
//   4. Receive partial results (real-time) + final result
//   5. Call recognizer.stopListening() or it auto-stops on silence
//
// REAL vs MOCK:
//   On a real device with Google STT installed, this works fully.
//   On emulator / CI: we detect availability and fall back to mock.
// ============================================================

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
import javax.inject.Singleton

// ── Speech recognition states ─────────────────────────────────
sealed class SpeechState {
    /** Microphone closed, nothing happening */
    data object Idle : SpeechState()

    /** Actively listening for speech input */
    data object Listening : SpeechState()

    /** Received partial (real-time) transcript being refined */
    data class Partial(val text: String) : SpeechState()

    /** Final recognised transcript ready */
    data class Result(val text: String) : SpeechState()

    /** Recognition stopped with no speech detected */
    data object NoSpeech : SpeechState()

    /** Something went wrong */
    data class Error(val message: String) : SpeechState()
}

@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Must run on Main thread — Android SpeechRecognizer requirement
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    /** Whether STT is available on this device */
    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    private var recognizer: SpeechRecognizer? = null

    // ── Mock data for environments without STT ────────────────
    private val mockPhrases = listOf(
        "Hello, how are you today?",
        "I need some help please",
        "Thank you very much",
        "Can you understand me?",
        "This is working great",
        "Nice to meet you",
        "Please speak slowly",
        "I am using SignLink"
    )
    private var mockIndex = 0
    private var mockJob: Job? = null

    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════

    /**
     * Start listening for speech.
     * On devices with STT: uses real SpeechRecognizer.
     * On emulator/no STT: uses mock simulation.
     */
    fun startListening() {
        if (_speechState.value is SpeechState.Listening) return

        if (isAvailable) {
            startRealRecognition()
        } else {
            startMockRecognition()
        }
    }

    /**
     * Stop listening immediately.
     */
    fun stopListening() {
        recognizer?.stopListening()
        mockJob?.cancel()
        _speechState.value = SpeechState.Idle
    }

    /**
     * Release all resources. Call from ViewModel.onCleared().
     */
    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        mockJob?.cancel()
        _speechState.value = SpeechState.Idle
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE: REAL SPEECH RECOGNITION
    // ═══════════════════════════════════════════════════════════

    private fun startRealRecognition() {
        mainScope.launch {
            // SpeechRecognizer MUST be created on the main thread
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                // Enable partial results for real-time text display
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Max time to wait for speech to start
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            }

            recognizer?.setRecognitionListener(object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    _speechState.value = SpeechState.Listening
                }

                override fun onBeginningOfSpeech() {
                    // User has started talking — already in Listening state
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed — could drive a VU meter UI in the future
                }

                override fun onBufferReceived(buffer: ByteArray?) { /* unused */ }

                override fun onEndOfSpeech() {
                    // Speech input ended — waiting for final result
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH          -> "Could not understand speech. Please try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT    -> "No speech detected. Please speak clearly."
                        SpeechRecognizer.ERROR_AUDIO             -> "Microphone error. Check mic permissions."
                        SpeechRecognizer.ERROR_NETWORK           -> "Network error. Check internet connection."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY   -> "Speech recognizer is busy. Try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                        else                                     -> "Recognition error ($error). Please try again."
                    }
                    // NO_MATCH is common on silence — treat as NoSpeech rather than error
                    _speechState.value = if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
                        SpeechState.NoSpeech
                    else
                        SpeechState.Error(message)
                }

                override fun onResults(results: Bundle?) {
                    // Final best result
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text    = matches?.firstOrNull() ?: ""
                    _speechState.value = if (text.isNotBlank())
                        SpeechState.Result(text)
                    else
                        SpeechState.NoSpeech
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Real-time partial transcript (updates as user speaks)
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: ""
                    if (partial.isNotBlank()) {
                        _speechState.value = SpeechState.Partial(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) { /* unused */ }
            })

            recognizer?.startListening(intent)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE: MOCK RECOGNITION (emulator / no STT service)
    // ═══════════════════════════════════════════════════════════

    private fun startMockRecognition() {
        _speechState.value = SpeechState.Listening
        mockJob?.cancel()

        mockJob = mainScope.launch {
            // Simulate "listening" for 1.5 seconds
            delay(1500)

            // Simulate partial results building up character by character
            val phrase = mockPhrases[mockIndex % mockPhrases.size]
            mockIndex++

            val words = phrase.split(" ")
            val partialBuilder = StringBuilder()

            for (word in words) {
                partialBuilder.append(if (partialBuilder.isEmpty()) word else " $word")
                _speechState.value = SpeechState.Partial(partialBuilder.toString())
                delay(200) // Each word appears 200ms apart
            }

            delay(300)
            // Final result
            _speechState.value = SpeechState.Result(phrase)
        }
    }
}