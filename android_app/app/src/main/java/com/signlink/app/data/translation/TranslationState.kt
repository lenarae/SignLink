// ============================================================
// File: data/translation/TranslationState.kt
// Purpose: Data models for the live translation feature.
//
// The translation pipeline is:
//   EMG+IMU Data → Gesture Classifier → Gesture Label
//   Gesture Label → Translation Text → TTS
//
// For now: Mock gesture stream (from BluetoothRepository)
//          → TranslationRepository
//          → TranslationViewModel
//          → TranslationScreen
// ============================================================

package com.signlink.app.data.translation

/**
 * The current status of the translation engine.
 * Shown in the status bar at the top of the translation screen.
 */
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
 * @param text       The translated word/phrase (e.g. "Hello")
 * @param confidence Classifier confidence 0.0–1.0 (mocked at 0.85–0.99)
 * @param timestampMs When this gesture was received
 */
data class TranslationEvent(
    val text:         String,
    val confidence:   Float,
    val timestampMs:  Long = System.currentTimeMillis()
) {
    /** Human-readable confidence label */
    val confidenceLabel: String
        get() = when {
            confidence >= 0.90f -> "High"
            confidence >= 0.75f -> "Medium"
            else                -> "Low"
        }
}