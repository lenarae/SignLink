package com.signlink.app.data.bluetooth


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


    data object Complete : CalibrationSessionState()
    data class Failed(val reason: String) : CalibrationSessionState()
}

sealed class CalibrationStepState {
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


    data object Done : CalibrationStepState()

    data class Error(val message: String) : CalibrationStepState()
}


data class CalibrationStep(
    val number:      Int,
    val title:       String,
    val description: String,
    val icon:        String,
    val durationMs:  Long
)


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