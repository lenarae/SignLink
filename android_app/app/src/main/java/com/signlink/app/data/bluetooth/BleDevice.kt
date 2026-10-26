// ============================================================
// File: data/bluetooth/BleDevice.kt
// Purpose: Data model representing one discovered BLE device.
//
// This is a simple data class — it just holds information.
// No logic, no Android SDK imports, easily testable.
//
// Fields match what Android's BluetoothDevice gives us,
// plus our app-specific fields (isSimulated, signalStrength).
// ============================================================

package com.signlink.app.data.bluetooth

/**
 * Represents a Bluetooth Low Energy device found during scanning.
 *
 * @param address       Unique MAC address, e.g. "AA:BB:CC:DD:EE:FF"
 * @param name          Device name broadcast by the peripheral.
 *                      Can be null if device hasn't advertised a name.
 * @param rssi          Signal strength in dBm. Range: -100 (far) to 0 (very close).
 *                      Typical usable range: -70 to -30 dBm.
 * @param isSimulated   true = this is our mock device for testing without hardware.
 *                      false = real physical device found via BLE scan.
 */
data class BleDevice(
    val address:     String,
    val name:        String?,
    val rssi:        Int,
    val isSimulated: Boolean = false
) {
    /**
     * Human-readable display name for the UI.
     * Falls back to the MAC address if no name was broadcast.
     */
    val displayName: String
        get() = when {
            !name.isNullOrBlank() -> name
            isSimulated           -> "SignLink Wristband (Simulated)"
            else                  -> "Unknown Device ($address)"
        }

    /**
     * Signal strength as a human-readable label.
     * Based on standard RSSI interpretation guidelines.
     */
    val signalLabel: String
        get() = when {
            rssi >= -50 -> "Excellent"
            rssi >= -60 -> "Good"
            rssi >= -70 -> "Fair"
            rssi >= -80 -> "Weak"
            else        -> "Very Weak"
        }

    /**
     * Signal strength as a 0.0–1.0 float for progress bars.
     * Maps the typical range -100..0 to 0..1.
     */
    val signalStrength: Float
        get() = ((rssi + 100f) / 100f).coerceIn(0f, 1f)

    /**
     * Whether this looks like a SignLink wristband.
     * Real wristband would advertise a name starting with "SignLink".
     */
    val isSignLinkDevice: Boolean
        get() = isSimulated || (name?.startsWith("SignLink", ignoreCase = true) == true)
}