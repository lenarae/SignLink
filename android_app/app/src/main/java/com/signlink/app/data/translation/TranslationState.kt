package com.signlink.app.data.translation


enum class TranslationStatus {
    /** Wristband connected, waiting for gesture input */
    LISTENING,

    /** A gesture was detected, classifier is computing */
    PROCESSING,

    /** Translation is paused by the user */
    PAUSED,

    /** Device disconnected mid-session */
    DISCONNECTED
}

/**
 * Represents a single translated gesture event.
 *
 * @param text
 * @param confidence
 * @param timestampMs
 */
data class TranslationEvent(
    val text:         String,
    val confidence:   Float,
    val timestampMs:  Long = System.currentTimeMillis()
) {

    val confidenceLabel: String
        get() = when {
            confidence >= 0.90f -> "High"
            confidence >= 0.75f -> "Medium"
            else                -> "Low"
        }
}