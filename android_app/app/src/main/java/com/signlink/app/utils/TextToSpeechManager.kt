package com.signlink.app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class TtsState {
    INITIALIZING,  // Engine is loading (can take 1-2 seconds)
    READY,         // Ready to speak
    SPEAKING,      // Currently speaking
    ERROR          // Initialization failed
}


@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ── State ─────────────────────────────────────────────────
    private val _ttsState = MutableStateFlow(TtsState.INITIALIZING)
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    // ── Settings ──────────────────────────────────────────────
    private var speechRate  = 1.0f
    private var speechPitch = 1.0f

    // ── The underlying TTS engine ────────────────────────────
    private var tts: TextToSpeech? = null

    init {
        // Initialize TTS engine — this is async (takes 0.5–2s)
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                _ttsState.value = if (
                    result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) TtsState.ERROR else TtsState.READY
            } else {
                _ttsState.value = TtsState.ERROR
            }

            // Progress listener updates our state
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _ttsState.value = TtsState.SPEAKING
                }
                override fun onDone(utteranceId: String?) {
                    _ttsState.value = TtsState.READY
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _ttsState.value = TtsState.READY
                }
            })
        }
    }

    /**
     * Speak the given text aloud.
     * Uses QUEUE_ADD so multiple calls queue up naturally.
     * @param text The text to speak
     * @param flushQueue If true, stops current speech first
     */
    fun speak(text: String, flushQueue: Boolean = false) {
        if (_ttsState.value == TtsState.ERROR) return
        tts?.apply {
            setSpeechRate(speechRate)
            setPitch(speechPitch)
            val queueMode = if (flushQueue) TextToSpeech.QUEUE_FLUSH
            else            TextToSpeech.QUEUE_ADD
            speak(text, queueMode, null, UUID.randomUUID().toString())
        }
    }

    /** Immediately stop any ongoing speech */
    fun stop() {
        tts?.stop()
        if (_ttsState.value == TtsState.SPEAKING) {
            _ttsState.value = TtsState.READY
        }
    }

    /**
     * Set speech rate.
     * @param rate 0.5 = half speed, 1.0 = normal, 2.0 = double speed
     */
    fun setRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
    }

    /**
     * Set speech pitch.
     * @param pitch 0.5 = deep, 1.0 = normal, 2.0 = high
     */
    fun setPitch(pitch: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
    }

    /** Release the TTS engine. Call when app is closing. */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}