// ============================================================
// File: data/bluetooth/CalibrationState.kt
// Purpose: Defines every possible state the calibration wizard can be in.
//
// The calibration process has 4 steps:
//   Step 1 → Wrist Position Check
//   Step 2 → Signal Quality Test
//   Step 3 → Gesture Baseline Recording
//   Step 4 → Confirmation
//
// Each step has sub-states: Idle, InProgress, Success, Failed.
// This sealed class captures all of them cleanly.
// ============================================================

package com.signlink.app.data.bluetooth

/**
 * Represents the overall calibration session status.
 * Used by CalibrationViewModel and CalibrationScreen.
 */
sealed class CalibrationSessionState {

    /** User hasn't started calibration yet */
    data object NotStarted : CalibrationSessionState()

    /**
     * Actively calibrating — holds which step we're on
     * @param step     Current step number (1–4)
     * @param stepState Current state of that step
     */
    data class InProgress(
        val step:      Int,
        val stepState: CalibrationStepState
    ) : CalibrationSessionState()

    /** All 4 steps completed successfully */
    data object Complete : CalibrationSessionState()

    /** Calibration failed and needs to be restarted */
    data class Failed(val reason: String) : CalibrationSessionState()
}

/**
 * State of an individual calibration step.
 */
sealed class CalibrationStepState {
    /** Step is queued but not started yet */
    data object Waiting : CalibrationStepState()

    /**
     * Step is actively running.
     * @param progress 0.0f–1.0f for the animated progress bar
     * @param message  Current status message shown to the user
     */
    data class Running(
        val progress: Float,
        val message:  String
    ) : CalibrationStepState()

    /** Step completed successfully */
    data object Done : CalibrationStepState()

    /** Step failed */
    data class Error(val message: String) : CalibrationStepState()
}

/**
 * Data class describing one calibration step's metadata.
 * Used to render step cards in the UI.
 */
data class CalibrationStep(
    val number:      Int,
    val title:       String,
    val description: String,
    val icon:        String,           // emoji icon for the step
    val durationMs:  Long              // how long this step takes (simulated)
)

/** All 4 calibration steps defined in one place */
val CALIBRATION_STEPS = listOf(
    CalibrationStep(
        number      = 1,
        title       = "Wrist Position",
        description = "Hold your wrist flat and relaxed, palm facing upward. The wristband LEDs should be in contact with your skin.",
        icon        = "🤚",
        durationMs  = 3_000
    ),
    CalibrationStep(
        number      = 2,
        title       = "Signal Quality",
        description = "Checking EMG and IMU sensor signal quality. Stay still while we verify the connection to each sensor electrode.",
        icon        = "📶",
        durationMs  = 4_000
    ),
    CalibrationStep(
        number      = 3,
        title       = "Gesture Baseline",
        description = "Now make a relaxed fist, then open your hand slowly. Repeat this 3 times to record your personal baseline.",
        icon        = "✊",
        durationMs  = 5_000
    ),
    CalibrationStep(
        number      = 4,
        title       = "Confirmation",
        description = "Verifying all calibration data. Your device will be fine-tuned to your unique hand profile.",
        icon        = "✅",
        durationMs  = 2_000
    )
)