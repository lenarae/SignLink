
package com.signlink.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signlink.app.data.bluetooth.BleDevice
import com.signlink.app.data.bluetooth.BluetoothRepository
import com.signlink.app.data.bluetooth.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {


    val connectionState: StateFlow<ConnectionState> =
        bluetoothRepository.connectionState

    val discoveredDevices: StateFlow<List<BleDevice>> =
        bluetoothRepository.discoveredDevices

    val gestureStream: SharedFlow<String> =
        bluetoothRepository.gestureStream

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()


    val errorMessage: StateFlow<String?> = connectionState
        .map { state ->
            if (state is ConnectionState.Failed) state.reason else null
        }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = null
        )



    val isScanning: StateFlow<Boolean> = connectionState
        .map { it is ConnectionState.Scanning }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isConnecting: StateFlow<Boolean> = connectionState
        .map { it is ConnectionState.Connecting }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isConnected: StateFlow<Boolean> = connectionState
        .map { it is ConnectionState.Connected }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)




    // USER ACTIONS (called by UI event handlers)
    fun startScan() {
        viewModelScope.launch {
            bluetoothRepository.startScan()
        }
    }


    fun stopScan() {
        bluetoothRepository.stopScan()
    }


    fun connect(device: BleDevice) {
        viewModelScope.launch {
            bluetoothRepository.connect(device)
        }
    }


    fun disconnect() {
        bluetoothRepository.disconnect()
    }


    fun retry() {
        bluetoothRepository.retry()
    }


    fun connectSimulated() {
        viewModelScope.launch {
            bluetoothRepository.connectSimulated()
        }
    }


    fun onPermissionsResult(granted: Boolean) {
        _permissionsGranted.value = granted
        if (granted) {
            // Auto-start scan once permissions are granted
            startScan()
        }
    }


    override fun onCleared() {
        super.onCleared()
        bluetoothRepository.stopScan()
    }
}