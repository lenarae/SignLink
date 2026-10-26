// ============================================================
// File: data/bluetooth/BleConstants.kt
// Purpose: All BLE-related magic numbers in one place.
//
// WHY a constants file?
//   If "SCAN_DURATION_MS" appears in 3 files, changing the timeout
//   means hunting through the codebase. Here: change once, done.
//
// When you integrate a real wristband, you'll:
//   1. Replace SERVICE_UUID with the device's actual GATT service UUID
//   2. Replace CHARACTERISTIC_UUID with the data characteristic UUID
//   These UUIDs come from the wristband manufacturer's documentation.
// ============================================================

package com.signlink.app.data.bluetooth

import java.util.UUID

object BleConstants {

    // ── Scan settings ─────────────────────────────────────────

    /** How long to scan for devices before automatically stopping (10 seconds) */
    const val SCAN_DURATION_MS = 10_000L

    /** Minimum time between scan restarts to avoid draining battery */
    const val SCAN_COOLDOWN_MS = 2_000L

    // ── GATT UUIDs ────────────────────────────────────────────
    // These are PLACEHOLDER UUIDs.
    // Replace with real UUIDs from your wristband's BLE specification.

    /** The primary GATT service UUID of the SignLink wristband */
    val SERVICE_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")

    /** The characteristic that streams EMG + IMU sensor data */
    val DATA_CHARACTERISTIC_UUID: UUID = UUID.fromString("87654321-4321-4321-4321-cba987654321")

    /** Client Characteristic Configuration Descriptor — used to enable notifications */
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // ── Simulated device ──────────────────────────────────────

    /** MAC address used for the mock/simulated device */
    const val SIMULATED_DEVICE_ADDRESS = "00:11:22:33:44:55"

    /** Name broadcast by the simulated device */
    const val SIMULATED_DEVICE_NAME    = "SignLink Wristband"

    /** RSSI for the simulated device (-45 dBm = "Excellent") */
    const val SIMULATED_DEVICE_RSSI    = -45

    // ── Mock gesture stream ───────────────────────────────────

    /** How often the simulated device emits a new gesture (milliseconds) */
    const val MOCK_GESTURE_INTERVAL_MS = 2_500L

    /** All gestures the simulation will cycle through */
    val MOCK_GESTURES = listOf(
        "Hello", "Yes", "No", "Thank you", "Please",
        "Help", "A", "B", "C", "I love you", "Good morning", "Goodbye"
    )
}