package com.signlink.app.data.bluetooth

import java.util.UUID

object BleConstants {

    // ── Scan settings ─────────────────────────────────────────
    const val SCAN_DURATION_MS = 10_000L
    const val SCAN_COOLDOWN_MS = 2_000L


    /** The primary GATT service UUID of the SignLink wristband */
    val SERVICE_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")
    /** The characteristic that streams EMG + IMU sensor data */
    val DATA_CHARACTERISTIC_UUID: UUID = UUID.fromString("87654321-4321-4321-4321-cba987654321")
    /** Client Characteristic Configuration Descriptor — used to enable notifications */
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")



    // ── Simulated device ──────────────────────────────────────
    const val SIMULATED_DEVICE_ADDRESS = "00:11:22:33:44:55"
    const val SIMULATED_DEVICE_NAME    = "SignLink Wristband"
    const val SIMULATED_DEVICE_RSSI    = -45


    // ── Mock gesture stream ───────────────────────────────────
    const val MOCK_GESTURE_INTERVAL_MS = 2_500L
    val MOCK_GESTURES = listOf(
        "Hello", "Yes", "No", "Thank you", "Please",
        "Help", "A", "B", "C", "I love you", "Good morning", "Goodbye", "Hope you're doing good"
    )
}