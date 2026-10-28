package com.signlink.app.data.bluetooth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ── Coroutine scope ───────────────────────────────────────
    // SupervisorJob: if one child coroutine fails, others keep running
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Connection state ──────────────────────────────────────
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // ── Discovered devices ────────────────────────────────────
    private val _discoveredDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDevice>> = _discoveredDevices.asStateFlow()

    // ── Gesture stream ────────────────────────────────────────
    private val _gestureStream = MutableSharedFlow<String>(replay = 1)
    val gestureStream: SharedFlow<String> = _gestureStream.asSharedFlow()

    // ── Internal state ────────────────────────────────────────
    private var scanJob:    Job? = null   // Reference to stop the scan
    private var streamJob:  Job? = null   // Reference to stop the stream
    private var mockGestureIndex = 0      // Cycles through MOCK_GESTURES list


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


    fun connect(device: BleDevice) {
        stopScan()
        _connectionState.value = ConnectionState.Connecting(device.displayName)

        scope.launch {
            // Simulate connection handshake delay
            delay(2000)

            if (device.isSignLinkDevice) {
                _connectionState.value = ConnectionState.Connected(device)
                startGestureStream()
            } else {
                delay(1000)
                _connectionState.value = ConnectionState.Failed(
                    "Device not recognized as a SignLink wristband. " +
                            "Make sure your device is powered on and in range."
                )
            }
        }
    }

    fun disconnect() {
        streamJob?.cancel()
        _connectionState.value = ConnectionState.Disconnected
        _gestureStream.resetReplayCache()
        // REAL BLE: bluetoothGatt?.disconnect(); bluetoothGatt?.close()
    }


    fun retry() {
        _connectionState.value = ConnectionState.Disconnected
        _discoveredDevices.value = emptyList()
        startScan()
    }


    fun connectSimulated() {
        val simulatedDevice = BleDevice(
            address     = BleConstants.SIMULATED_DEVICE_ADDRESS,
            name        = BleConstants.SIMULATED_DEVICE_NAME,
            rssi        = BleConstants.SIMULATED_DEVICE_RSSI,
            isSimulated = true
        )
        connect(simulatedDevice)
    }


    private fun addMockDevice(device: BleDevice) {
        val current = _discoveredDevices.value.toMutableList()
        if (current.none { it.address == device.address }) {
            // Randomize RSSI slightly to simulate real signal fluctuation
            val jitteredRssi = device.rssi + Random.nextInt(-5, 5)
            current.add(device.copy(rssi = jitteredRssi))
            _discoveredDevices.value = current
        }
    }


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