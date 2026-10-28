package com.signlink.app.utils

import android.bluetooth.BluetoothAdapter
import java.io.IOException
import java.net.UnknownHostException

/**
 * Classification of error types for appropriate UI treatment.
 */
enum class ErrorType {
    BLUETOOTH_OFF,        // BT adapter disabled
    BLUETOOTH_PERMISSION, // Permission not granted
    BLUETOOTH_TIMEOUT,    // Connection timed out
    NO_DEVICE_FOUND,      // Scan found nothing
    DEVICE_DISCONNECTED,  // Was connected, now lost
    MICROPHONE_PERMISSION,// Mic permission denied
    DATABASE_ERROR,       // Room DB failure
    TTS_UNAVAILABLE,      // TTS engine not installed
    NETWORK_ERROR,        // (Future) API unreachable
    UNKNOWN               // Catch-all
}

/**
 * A structured error that carries a user-friendly message and action.
 *
 * @param type          What category of error this is
 * @param title         Short headline (shown in bold)
 * @param message       Friendly explanation (no tech jargon)
 * @param actionLabel   Label for the primary recovery button (null = no button)
 */
data class AppError(
    val type:        ErrorType,
    val title:       String,
    val message:     String,
    val actionLabel: String? = null
)

object ErrorHandler {

    /**
     * Convert a raw exception into a user-friendly AppError.
     */
    fun fromException(exception: Throwable): AppError = when (exception) {
        is SecurityException -> AppError(
            type        = ErrorType.BLUETOOTH_PERMISSION,
            title       = "Permission needed",
            message     = "SignLink needs Bluetooth permission to connect to your wristband. Tap below to open Settings.",
            actionLabel = "Open Settings"
        )
        is IOException -> AppError(
            type        = ErrorType.DATABASE_ERROR,
            title       = "Storage error",
            message     = "Could not save to local storage. Please check that the app has storage access.",
            actionLabel = "Retry"
        )
        is UnknownHostException -> AppError(
            type        = ErrorType.NETWORK_ERROR,
            title       = "No internet connection",
            message     = "Check your network and try again.",
            actionLabel = "Retry"
        )
        else -> AppError(
            type        = ErrorType.UNKNOWN,
            title       = "Something went wrong",
            message     = exception.message ?: "An unexpected error occurred. Please try again.",
            actionLabel = "Retry"
        )
    }

    /**
     * Create a pre-defined AppError for known situations.
     */
    fun bluetoothOff() = AppError(
        type        = ErrorType.BLUETOOTH_OFF,
        title       = "Bluetooth is off",
        message     = "Turn on Bluetooth to scan for and connect to your wristband.",
        actionLabel = "Enable Bluetooth"
    )

    fun noDeviceFound() = AppError(
        type        = ErrorType.NO_DEVICE_FOUND,
        title       = "No devices found",
        message     = "Make sure your wristband is powered on and within 10 metres. Try scanning again.",
        actionLabel = "Scan again"
    )

    fun deviceDisconnected(deviceName: String) = AppError(
        type        = ErrorType.DEVICE_DISCONNECTED,
        title       = "Wristband disconnected",
        message     = "$deviceName has disconnected. Move closer to the device and reconnect.",
        actionLabel = "Reconnect"
    )

    fun microphonePermissionDenied() = AppError(
        type        = ErrorType.MICROPHONE_PERMISSION,
        title       = "Microphone access needed",
        message     = "SignLink needs microphone access for speech-to-text. Grant permission in Settings.",
        actionLabel = "Open Settings"
    )

    fun ttsUnavailable() = AppError(
        type        = ErrorType.TTS_UNAVAILABLE,
        title       = "Text-to-speech unavailable",
        message     = "No text-to-speech engine was found on this device. Install Google Text-to-Speech from the Play Store.",
        actionLabel = "Get TTS engine"
    )
}