package com.signlink.app.data.translation

import com.signlink.app.data.bluetooth.BluetoothRepository
import com.signlink.app.data.bluetooth.ConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TranslationRepository @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Translation status ────────────────────────────────────
    private val _status = MutableStateFlow(TranslationStatus.DISCONNECTED)
    val status: StateFlow<TranslationStatus> = _status.asStateFlow()

    // ── Translation event stream ──────────────────────────────
    // replay=0: only new subscribers get new events (stream, not history)
    private val _events = MutableSharedFlow<TranslationEvent>(replay = 0)
    val events: SharedFlow<TranslationEvent> = _events.asSharedFlow()

    // ── Session text accumulator ──────────────────────────────
    // All translated words joined for the full-session display
    private val _sessionText = MutableStateFlow("")
    val sessionText: StateFlow<String> = _sessionText.asStateFlow()

    // ── Translation paused flag ───────────────────────────────
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    init {
        // Observe BLE connection state → update translation status
        scope.launch {
            bluetoothRepository.connectionState.collect { connState ->
                _status.value = when (connState) {
                    is ConnectionState.Connected    -> if (_isPaused.value)
                        TranslationStatus.PAUSED else TranslationStatus.LISTENING
                    is ConnectionState.Disconnected -> TranslationStatus.DISCONNECTED
                    else                            -> TranslationStatus.DISCONNECTED
                }
            }
        }

        // Observe the BLE gesture stream → produce translation events
        scope.launch {
            bluetoothRepository.gestureStream.collect { gestureLabel ->
                if (_isPaused.value) return@collect
                if (bluetoothRepository.connectionState.value !is ConnectionState.Connected) return@collect

                // Show PROCESSING status briefly (simulates ML inference)
                _status.value = TranslationStatus.PROCESSING
                delay(300) // Mock inference latency

                // Create a TranslationEvent with a mock confidence score
                val event = TranslationEvent(
                    text       = gestureLabel,
                    confidence = 0.82f + Random.nextFloat() * 0.17f  // 0.82–0.99
                )

                _events.emit(event)

                // Append to session text
                val currentText = _sessionText.value
                _sessionText.value = if (currentText.isEmpty()) gestureLabel
                else "$currentText $gestureLabel"

                // Return to LISTENING
                _status.value = TranslationStatus.LISTENING
            }
        }
    }


    fun togglePause() {
        _isPaused.value = !_isPaused.value
        _status.value = if (_isPaused.value) TranslationStatus.PAUSED
        else                 TranslationStatus.LISTENING
    }

    fun clearSession() {
        _sessionText.value = ""
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
        if (!paused) _status.value = TranslationStatus.LISTENING
        else         _status.value = TranslationStatus.PAUSED
    }
}