// ============================================================
// File: data/bluetooth/BluetoothRepository.kt
// Purpose: ALL Bluetooth logic lives here. This is the "brain"
// of the BLE module.
//
// ARCHITECTURE NOTE (MVVM):
//   ViewModel asks Repository to do things.
//   Repository emits state via StateFlow/SharedFlow.
//   ViewModel exposes those flows to the UI.
//   UI NEVER talks directly to the repository.
//
// CURRENT IMPLEMENTATION:
//   Phase 3 simulates BLE for two reasons:
//     1. You can test without owning the wristband hardware
//     2. The architecture is identical — swap mock code for real
//        BluetoothLeScanner calls when hardware is available
//
// REAL BLE INTEGRATION (Future):
//   When ready, replace startScan() body with real scanner code.
//   Everything else (ViewModel, UI) stays exactly the same.
// ============================================================

package com.signlink.app.data.bluetooth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repository that manages BLE scanning, device connection,
 * and streaming gesture data from the wristband.
 *
 * @Singleton means only ONE instance exists for the whole app lifetime.
 * @Inject tells Hilt to create this automatically (no manual `new`).
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ── Coroutine scope ───────────────────────────────────────
    // SupervisorJob: if one child coroutine fails, others keep running
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Connection state ──────────────────────────────────────
    // MutableStateFlow = observable value that can change over time
    // Exposed as read-only StateFlow to ViewModels
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // ── Discovered devices ────────────────────────────────────
    // List of BLE devices found during scanning
    private val _discoveredDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDevice>> = _discoveredDevices.asStateFlow()

    // ── Gesture stream ────────────────────────────────────────
    // SharedFlow = hot stream; emits events to all current collectors
    // replay=1: new subscribers immediately see the last gesture
    private val _gestureStream = MutableSharedFlow<String>(replay = 1)
    val gestureStream: SharedFlow<String> = _gestureStream.asSharedFlow()

    // ── Internal state ────────────────────────────────────────
    private var scanJob:    Job? = null   // Reference to stop the scan
    private var streamJob:  Job? = null   // Reference to stop the stream
    private var mockGestureIndex = 0      // Cycles through MOCK_GESTURES list

    // ══════════════════════════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════════════════════════

    /**
     * Start scanning for nearby BLE devices.
     *
     * Emits state: Disconnected → Scanning → (auto-stops after 10s)
     *
     * MOCK BEHAVIOR:
     *   - First finds 2 generic "other" devices immediately
     *   - After 3 seconds, finds the SignLink wristband
     *   - Scan stops after SCAN_DURATION_MS (10 seconds)
     *
     * REAL BLE (Future replacement):
     *   val scanner = bluetoothAdapter.bluetoothLeScanner
     *   scanner.startScan(filters, settings, scanCallback)
     */
    fun startScan() {
        // Don't start a new scan if already scanning or connected
        if (_connectionState.value is ConnectionState.Scanning) return

        scanJob?.cancel()
        _discoveredDevices.value = emptyList()
        _connectionState.value   = ConnectionState.Scanning

        scanJob = scope.launch {
            // ── Simulate finding nearby generic devices ────────
            delay(800)
            addMockDevice(
                BleDevice(
                    address = "AA:11:22:33:44:55",
                    name    = "Galaxy Buds",
                    rssi    = -72
                )
            )
            delay(1200)
            addMockDevice(
                BleDevice(
                    address = "BB:22:33:44:55:66",
                    name    = "Xiaomi Band 8",
                    rssi    = -68
                )
            )

            // ── Simulate finding the SignLink wristband ────────
            delay(1500)
            addMockDevice(
                BleDevice(
                    address     = BleConstants.SIMULATED_DEVICE_ADDRESS,
                    name        = BleConstants.SIMULATED_DEVICE_NAME,
                    rssi        = BleConstants.SIMULATED_DEVICE_RSSI,
                    isSimulated = true
                )
            )

            // ── Auto-stop after scan duration ─────────────────
            delay(BleConstants.SCAN_DURATION_MS - 3_500)
            if (_connectionState.value is ConnectionState.Scanning) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    /**
     * Stop any ongoing BLE scan without connecting.
     */
    fun stopScan() {
        scanJob?.cancel()
        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    /**
     * Connect to a specific BLE device.
     *
     * Emits state: Connecting(name) → Connected(device) or Failed(reason)
     *
     * MOCK BEHAVIOR:
     *   - Shows "Connecting..." for 2 seconds
     *   - Always succeeds for the simulated device
     *   - Non-SignLink devices fail after 3 seconds
     *   - After connecting: starts mock gesture stream
     *
     * REAL BLE (Future):
     *   device.bluetoothDevice.connectGatt(context, false, gattCallback)
     *   Then in gattCallback.onConnectionStateChange() → emit Connected/Failed
     */
    fun connect(device: BleDevice) {
        stopScan()
        _connectionState.value = ConnectionState.Connecting(device.displayName)

        scope.launch {
            // Simulate connection handshake delay
            delay(2000)

            if (device.isSignLinkDevice) {
                // ✅ Connection success
                _connectionState.value = ConnectionState.Connected(device)
                // Start streaming mock gesture data
                startGestureStream()
            } else {
                // ❌ Non-SignLink device: show error
                delay(1000)
                _connectionState.value = ConnectionState.Failed(
                    "Device not recognized as a SignLink wristband. " +
                            "Make sure your device is powered on and in range."
                )
            }
        }
    }

    /**
     * Disconnect from the current device and clean up.
     */
    fun disconnect() {
        streamJob?.cancel()
        _connectionState.value = ConnectionState.Disconnected
        _gestureStream.resetReplayCache()
        // REAL BLE: bluetoothGatt?.disconnect(); bluetoothGatt?.close()
    }

    /**
     * Retry connection after a failure.
     * Resets state and re-runs the scan.
     */
    fun retry() {
        _connectionState.value = ConnectionState.Disconnected
        _discoveredDevices.value = emptyList()
        startScan()
    }

    /**
     * Connect to the simulated device directly (skips scan).
     * Useful for testing and for the "Demo Mode" button.
     */
    fun connectSimulated() {
        val simulatedDevice = BleDevice(
            address     = BleConstants.SIMULATED_DEVICE_ADDRESS,
            name        = BleConstants.SIMULATED_DEVICE_NAME,
            rssi        = BleConstants.SIMULATED_DEVICE_RSSI,
            isSimulated = true
        )
        connect(simulatedDevice)
    }

    // ══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════

    /**
     * Add a device to the discovered list (no duplicates).
     */
    private fun addMockDevice(device: BleDevice) {
        val current = _discoveredDevices.value.toMutableList()
        if (current.none { it.address == device.address }) {
            // Randomize RSSI slightly to simulate real signal fluctuation
            val jitteredRssi = device.rssi + Random.nextInt(-5, 5)
            current.add(device.copy(rssi = jitteredRssi))
            _discoveredDevices.value = current
        }
    }

    /**
     * Start emitting mock gesture events on a fixed interval.
     *
     * In a real implementation, this would be replaced by
     * BLE GATT notifications from the wristband characteristic.
     * The SharedFlow emission API stays exactly the same.
     */
    private fun startGestureStream() {
        streamJob?.cancel()
        streamJob = scope.launch {
            while (isActive) {
                delay(BleConstants.MOCK_GESTURE_INTERVAL_MS)
                val gesture = BleConstants.MOCK_GESTURES[
                    mockGestureIndex % BleConstants.MOCK_GESTURES.size
                ]
                mockGestureIndex++
                _gestureStream.emit(gesture)
            }
        }
    }
}