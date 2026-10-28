package com.signlink.app.data.bluetooth


sealed class ConnectionState {


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