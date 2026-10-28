package com.signlink.app.data.bluetooth

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class CalibrationRepository @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Session state ─────────────────────────────────────────
    private val _sessionState = MutableStateFlow<CalibrationSessionState>(
        CalibrationSessionState.NotStarted
    )
    val sessionState: StateFlow<CalibrationSessionState> = _sessionState.asStateFlow()

    // ── Signal strength (mocked EMG/IMU reading) ──────────────
    // Fluctuates in real-time during calibration for visual feedback
    private val _signalStrength = MutableStateFlow(0f)
    val signalStrength: StateFlow<Float> = _signalStrength.asStateFlow()

    // ── Channel quality per electrode (mocked) ────────────────
    // 8 EMG channels — each shows signal quality 0–100%
    private val _channelQualities = MutableStateFlow(List(8) { 0f })
    val channelQualities: StateFlow<List<Float>> = _channelQualities.asStateFlow()

    private var calibrationJob: Job? = null


    /** Begin the full calibration sequence from Step 1. */
    fun startCalibration() {
        calibrationJob?.cancel()
        _signalStrength.value  = 0f
        _channelQualities.value = List(8) { 0f }

        calibrationJob = scope.launch {
            for (step in CALIBRATION_STEPS) {
                val success = runStep(step)
                if (!success) {
                    _sessionState.value = CalibrationSessionState.Failed(
                        "Step ${step.number} failed: signal quality too low. " +
                                "Please ensure the wristband is snug against your skin."
                    )
                    return@launch
                }
            }
            // All steps passed
            _sessionState.value = CalibrationSessionState.Complete
            _signalStrength.value = 1f
        }
    }

    /** Cancel and reset calibration. */
    fun reset() {
        calibrationJob?.cancel()
        _sessionState.value     = CalibrationSessionState.NotStarted
        _signalStrength.value   = 0f
        _channelQualities.value = List(8) { 0f }
    }

    /** Retry after failure — restarts from Step 1. */
    fun retry() = startCalibration()

    // ══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════

    /**
     * Runs a single calibration step, animating progress and signal.
     * Returns true if the step succeeded, false if it should fail.
     */
    private suspend fun runStep(step: CalibrationStep): Boolean {
        val messages = stepMessages(step.number)
        val tickMs   = 100L
        val ticks    = (step.durationMs / tickMs).toInt()

        for (i in 0..ticks) {
            val progress = i.toFloat() / ticks
            val msgIndex = (progress * messages.size).toInt().coerceAtMost(messages.size - 1)

            // Update session state with current progress
            _sessionState.value = CalibrationSessionState.InProgress(
                step      = step.number,
                stepState = CalibrationStepState.Running(
                    progress = progress,
                    message  = messages[msgIndex]
                )
            )

            // Animate signal strength — rises with some noise
            val targetSignal = 0.3f + (progress * 0.7f)
            val noise        = Random.nextFloat() * 0.08f - 0.04f
            _signalStrength.value = (targetSignal + noise).coerceIn(0f, 1f)

            // Animate channel qualities (each activates progressively)
            if (step.number == 2) {
                _channelQualities.value = List(8) { channelIndex ->
                    val activateAt = channelIndex / 8f
                    if (progress > activateAt) {
                        val channelNoise = Random.nextFloat() * 0.1f
                        (0.75f + channelNoise + (progress - activateAt) * 0.2f).coerceIn(0f, 1f)
                    } else 0f
                }
            }

            delay(tickMs)
        }

        // Mark step as Done
        _sessionState.value = CalibrationSessionState.InProgress(
            step      = step.number,
            stepState = CalibrationStepState.Done
        )
        delay(400) // Brief pause so user sees the checkmark

        // Steps 1–4 always succeed in simulation
        // Real implementation: return false if SNR < threshold, etc.
        return true
    }






    private fun stepMessages(stepNumber: Int): List<String> = when (stepNumber) {
        1 -> listOf(
            "Detecting wrist contact...",
            "Measuring electrode pressure...",
            "Wrist position confirmed ✓"
        )
        2 -> listOf(
            "Checking electrode contact...",
            "Measuring EMG noise floor...",
            "Reading IMU gyroscope...",
            "Verifying accelerometer...",
            "Signal quality confirmed ✓"
        )
        3 -> listOf(
            "Recording open hand baseline...",
            "Recording closed fist baseline...",
            "Capturing motion profile...",
            "Analyzing gesture range...",
            "Baseline recorded ✓"
        )
        4 -> listOf(
            "Computing calibration matrix...",
            "Calibration complete ✓"
        )
        else -> listOf("Processing...")
    }
}