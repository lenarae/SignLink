package com.signlink.app.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter = bluetoothManager?.adapter
    private val bluetoothLeScanner: BluetoothLeScanner?
        get() = bluetoothAdapter?.bluetoothLeScanner

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDevice>> = _discoveredDevices.asStateFlow()

    private val _gestureStream = MutableSharedFlow<String>(replay = 1)
    val gestureStream: SharedFlow<String> = _gestureStream.asSharedFlow()

    private var scanJob: Job? = null
    private var streamJob: Job? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var mockGestureIndex = 0

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = try {
                device.name
            } catch (_: SecurityException) {
                null
            } ?: result.scanRecord?.deviceName
            val address = device.address
            val rssi = result.rssi

            val bleDevice = BleDevice(
                address = address,
                name = name,
                rssi = rssi,
                isSimulated = false
            )
            addDiscoveredDevice(bleDevice)
        }

        override fun onScanFailed(errorCode: Int) {
            // handle scan error if needed
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (_connectionState.value is ConnectionState.Scanning) return

        // Check if Bluetooth is enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = ConnectionState.Failed(
                "Bluetooth is turned off. Please turn on Bluetooth in your device settings."
            )
            return
        }

        scanJob?.cancel()
        _discoveredDevices.value = emptyList()
        _connectionState.value = ConnectionState.Scanning

        // 1. Add already bonded / paired devices first
        try {
            bluetoothAdapter.bondedDevices?.forEach { device ->
                val bondedBleDevice = BleDevice(
                    address = device.address,
                    name = device.name,
                    rssi = -50,
                    isSimulated = false
                )
                addDiscoveredDevice(bondedBleDevice)
            }
        } catch (_: SecurityException) {
            // Missing BLUETOOTH_CONNECT permission
        }

        // 2. Start BLE scanning
        try {
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            bluetoothLeScanner?.startScan(null, settings, scanCallback)
        } catch (_: SecurityException) {
            _connectionState.value = ConnectionState.Failed(
                "Bluetooth permissions not granted. Please grant scan permission."
            )
            return
        }

        // Auto-stop scan after duration
        scanJob = scope.launch {
            delay(BleConstants.SCAN_DURATION_MS)
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: SecurityException) {
            // ignore
        }
        scanJob?.cancel()
        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BleDevice) {
        stopScan()

        if (device.isSimulated) {
            connectSimulatedInternal(device)
            return
        }

        _connectionState.value = ConnectionState.Connecting(device.displayName)

        scope.launch {
            try {
                val btDevice = bluetoothAdapter?.getRemoteDevice(device.address)
                if (btDevice == null) {
                    _connectionState.value = ConnectionState.Failed("Device not found.")
                    return@launch
                }

                try {
                    bluetoothGatt?.close()
                } catch (_: Exception) {}

                bluetoothGatt = btDevice.connectGatt(context, false, object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            _connectionState.value = ConnectionState.Connected(device)
                            try {
                                gatt.discoverServices()
                            } catch (_: SecurityException) { }
                            startGestureStream()
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            _connectionState.value = ConnectionState.Disconnected
                            try {
                                bluetoothGatt?.close()
                            } catch (_: Exception) {}
                            bluetoothGatt = null
                            streamJob?.cancel()
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        // Services discovered
                    }
                }, BluetoothDevice.TRANSPORT_LE)

            } catch (e: SecurityException) {
                _connectionState.value = ConnectionState.Failed(
                    "Bluetooth connect permission missing: ${e.localizedMessage}"
                )
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Failed(
                    "Connection failed: ${e.localizedMessage}"
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        streamJob?.cancel()
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (_: Exception) { }
        bluetoothGatt = null
        _connectionState.value = ConnectionState.Disconnected
        _gestureStream.resetReplayCache()
    }

    fun retry() {
        disconnect()
        startScan()
    }

    fun connectSimulated() {
        val simulatedDevice = BleDevice(
            address = BleConstants.SIMULATED_DEVICE_ADDRESS,
            name = BleConstants.SIMULATED_DEVICE_NAME,
            rssi = BleConstants.SIMULATED_DEVICE_RSSI,
            isSimulated = true
        )
        connect(simulatedDevice)
    }

    private fun connectSimulatedInternal(device: BleDevice) {
        _connectionState.value = ConnectionState.Connecting(device.displayName)
        scope.launch {
            delay(2000)
            _connectionState.value = ConnectionState.Connected(device)
            startGestureStream()
        }
    }

    private fun addDiscoveredDevice(device: BleDevice) {
        val current = _discoveredDevices.value.toMutableList()
        if (current.none { it.address == device.address }) {
            current.add(device)
            _discoveredDevices.value = current
        } else {
            val index = current.indexOfFirst { it.address == device.address }
            if (index != -1) {
                current[index] = current[index].copy(rssi = device.rssi, name = device.name ?: current[index].name)
                _discoveredDevices.value = current
            }
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
