// ============================================================
// File: utils/SpeechRecognizerManager.kt
// Purpose: Wraps Android's SpeechRecognizer into a clean,
// coroutine/Flow-based API that the ViewModel can observe.
//
// ── CRITICAL BUG FIXED ────────────────────────────────────────
// BEFORE: Was @Singleton — SpeechRecognizer held for entire app
//         lifetime → "recognizer busy" errors on re-entry.
// AFTER:  NOT @Singleton. One instance per ViewModel scope.
//         destroy() frees it when ViewModel.onCleared() fires.
//
// THREADING: SpeechRecognizer MUST run on Main thread.
//            This class handles that internally.
//
// REAL vs MOCK:
//   Real device with Google STT → SpeechRecognizer used fully.
//   Emulator / no STT service   → mock word-by-word simulation.
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

// ── Speech State sealed class ─────────────────────────────────
/**
 * Every possible state the speech recogniser can be in.
 * The UI renders different visuals for each state.
 */
sealed class SpeechState {
    /** Mic is off. Nothing happening. */
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

    /** Listened but heard nothing. Not an error — just silence. */
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

    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════

    /**
     * Begin listening for speech.
     * Call AFTER confirming RECORD_AUDIO permission is granted.
     * No-op if already listening.
     */
    fun startListening() {
        val current = _speechState.value
        if (current is SpeechState.Listening || current is SpeechState.Partial) return

        if (isAvailable) startRealRecognition()
        else                        startMockRecognition()
    }

    /**
     * Stop listening and reset to Idle.
     * Safe to call even if not listening.
     */
    fun stopListening() {
        recognizer?.stopListening()
        mockJob?.cancel()
        _speechState.value = SpeechState.Idle
    }

    /**
     * Release ALL resources. MUST call from ViewModel.onCleared().
     * After this, do not call startListening() again.
     */
    fun destroy() {
        mockJob?.cancel()
        mainScope.cancel()
        recognizer?.destroy()
        recognizer = null
        _speechState.value = SpeechState.Idle
    }

    // ═══════════════════════════════════════════════════════════
    // REAL RECOGNITION
    // ═══════════════════════════════════════════════════════════

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

    /**
     * Build RecognitionListener mapping Android callbacks → SpeechState.
     * All 8 methods are implemented even if they're no-ops, as Android requires.
     */
    private fun buildListener() = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            // Engine ready — mic is now active
            _speechState.value = SpeechState.Listening
        }

        override fun onBeginningOfSpeech() {
            // User started talking — stay in Listening, Partial will follow
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Amplitude change — could drive a waveform visualiser
            // rmsdB ≈ -2 to 10 dBm. Not used for now.
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Raw audio bytes — not needed for text recognition
        }

        override fun onEndOfSpeech() {
            // User stopped talking — engine is processing
            // onResults() or onError() comes next
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Real-time partial text — called repeatedly while user speaks
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()         // index 0 = highest confidence
                ?: return

            if (partial.isNotBlank()) _speechState.value = SpeechState.Partial(partial)
        }

        override fun onResults(results: Bundle?) {
            // Final result — user has stopped speaking
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
            // Map integer error codes to friendly English messages
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

            // NO_MATCH / SPEECH_TIMEOUT are "nothing said", not real errors
            _speechState.value = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechState.NoSpeech
                else                                  -> SpeechState.Error(message, error)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Reserved by Android for future use — not triggered in practice
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MOCK RECOGNITION
    // ═══════════════════════════════════════════════════════════

    /**
     * Simulates realistic word-by-word partial results + final Result.
     * Used when Google STT is not available (emulator, CI, etc).
     */
    private fun startMockRecognition() {
        _speechState.value = SpeechState.Listening
        mockJob?.cancel()

        mockJob = mainScope.launch {
            delay(1_200)  // Simulate user thinking before speaking

            val phrase = mockPhrases[mockIndex % mockPhrases.size]
            mockIndex++

            // Emit each word progressively — simulates partial results
            val words = phrase.split(" ")
            val sb = StringBuilder()
            for (word in words) {
                if (!isActive) return@launch
                sb.append(if (sb.isEmpty()) word else " $word")
                _speechState.value = SpeechState.Partial(sb.toString())
                delay(180)  // ~180ms per word — realistic speech pace
            }

            delay(350)  // Brief pause before final lock-in
            _speechState.value = SpeechState.Result(phrase)
        }
    }
}