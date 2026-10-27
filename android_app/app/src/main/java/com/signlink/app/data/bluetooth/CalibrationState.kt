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

/** All 5 calibration steps defined in one place */
val CALIBRATION_STEPS = listOf(
    CalibrationStep(
        number      = 1,
        title       = "Rest Position",
        description = "Keep your hand relaxed at your side",
        icon        = "🤚",
        durationMs  = 3_000
    ),
    CalibrationStep(
        number      = 2,
        title       = "Fist Gesture",
        description = "Make a fist with your hand",
        icon        = "✊",
        durationMs  = 3_000
    ),
    CalibrationStep(
        number      = 3,
        title       = "Open Palm",
        description = "Open your hand with palm facing forward",
        icon        = "🖐️",
        durationMs  = 3_000
    ),
    CalibrationStep(
        number      = 4,
        title       = "Point Gesture",
        description = "Point your index finger upward",
        icon        = "☝️",
        durationMs  = 3_000
    ),
    CalibrationStep(
        number      = 5,
        title       = "Wave Motion",
        description = "Wave your hand side to side",
        icon        = "👋",
        durationMs  = 3_000
    )
)