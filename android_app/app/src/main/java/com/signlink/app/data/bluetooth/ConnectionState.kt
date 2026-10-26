// ============================================================
// File: data/bluetooth/ConnectionState.kt
// Purpose: Represent all possible BLE connection states.
//
// Using a sealed class means:
//   - The compiler knows EVERY possible state
//   - when() expressions are exhaustive (no "else" needed)
//   - States can carry data (e.g. error message in Failed)
//
// This pattern is used throughout the app for UI state machines.
// ============================================================

package com.signlink.app.data.bluetooth

/**
 * Sealed class representing every possible state of the
 * BLE connection between the app and the wristband.
 *
 * Used by:
 *   - BluetoothRepository (produces state changes)
 *   - BluetoothViewModel (holds as StateFlow)
 *   - BluetoothScreen (observes and renders UI accordingly)
 */
sealed class ConnectionState {

    /**
     * No connection. App has never connected or was disconnected.
     * This is the default/initial state.
     */
    data object Disconnected : ConnectionState()

    /**
     * Actively scanning for nearby BLE devices.
     * Scan lasts up to [BleConstants.SCAN_DURATION_MS] ms.
     */
    data object Scanning : ConnectionState()

    /**
     * Found a device and attempting to establish GATT connection.
     * @param deviceName Name of the device being connected to
     */
    data class Connecting(val deviceName: String) : ConnectionState()

    /**
     * Successfully connected to the wristband.
     * @param device The connected BleDevice (for displaying info)
     */
    data class Connected(val device: BleDevice) : ConnectionState()

    /**
     * Connection attempt failed or dropped unexpectedly.
     * @param reason Human-readable explanation shown in the UI.
     */
    data class Failed(val reason: String) : ConnectionState()
}