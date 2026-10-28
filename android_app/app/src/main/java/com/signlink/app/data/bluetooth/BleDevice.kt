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

    val displayName: String
        get() = when {
            !name.isNullOrBlank() -> name
            isSimulated           -> "SignLink Wristband (Simulated)"
            else                  -> "Unknown Device ($address)"
        }

    val signalLabel: String
        get() = when {
            rssi >= -50 -> "Excellent"
            rssi >= -60 -> "Good"
            rssi >= -70 -> "Fair"
            rssi >= -80 -> "Weak"
            else        -> "Very Weak"
        }

    val signalStrength: Float
        get() = ((rssi + 100f) / 100f).coerceIn(0f, 1f)


    val isSignLinkDevice: Boolean
        get() = isSimulated || (name?.startsWith("SignLink", ignoreCase = true) == true)
}