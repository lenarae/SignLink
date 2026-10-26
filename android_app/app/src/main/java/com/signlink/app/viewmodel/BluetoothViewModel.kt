// ============================================================
// File: viewmodel/BluetoothViewModel.kt
// Purpose: The ViewModel for all Bluetooth screens.
//
// WHAT A VIEWMODEL DOES:
//   - Survives screen rotation (unlike a regular Composable)
//   - Holds UI state that should persist while the screen is alive
//   - Calls the Repository (never touches the UI directly)
//   - Exposes StateFlows that the UI observes
//
// MVVM DATA FLOW:
//   UI click → ViewModel function → Repository action
//   Repository state change → ViewModel StateFlow updates → UI recomposes
//
// @HiltViewModel tells Hilt to auto-create this ViewModel.
// @Inject constructor means Hilt injects BluetoothRepository for us.
// ============================================================

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

/**
 * ViewModel for the Bluetooth screen.
 *
 * Exposes:
 *   - connectionState: current BLE connection state
 *   - discoveredDevices: list of devices found during scan
 *   - gestureStream: incoming gesture translations
 *   - permissionsGranted: whether user has given BT permissions
 */
@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {

    // ── Expose repository streams directly to the UI ──────────
    // The UI observes these as read-only StateFlows.
    // collectAsStateWithLifecycle() in Compose turns them into Compose State.

    val connectionState: StateFlow<ConnectionState> =
        bluetoothRepository.connectionState

    val discoveredDevices: StateFlow<List<BleDevice>> =
        bluetoothRepository.discoveredDevices

    val gestureStream: SharedFlow<String> =
        bluetoothRepository.gestureStream

    // ── Permission state ──────────────────────────────────────
    // Tracks whether Bluetooth permissions have been granted.
    // Updated by the UI when the permission dialog result comes back.
    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    // ── Last error message for display ───────────────────────
    // Extracted from ConnectionState.Failed for convenience
    val errorMessage: StateFlow<String?> = connectionState
        .map { state ->
            if (state is ConnectionState.Failed) state.reason else null
        }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = null
        )

    // ── Convenience booleans for UI ───────────────────────────
    // Makes Composable if() conditions simpler to read

    val isScanning: StateFlow<Boolean> = connectionState
        .map { it is ConnectionState.Scanning }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isConnecting: StateFlow<Boolean> = connectionState
        .map { it is ConnectionState.Connecting }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isConnected: StateFlow<Boolean> = connectionState
        .map { it is ConnectionState.Connected }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ══════════════════════════════════════════════════════════
    // USER ACTIONS (called by UI event handlers)
    // ══════════════════════════════════════════════════════════

    /**
     * Start scanning for BLE devices nearby.
     * Called when user taps "Scan for Devices".
     */
    fun startScan() {
        viewModelScope.launch {
            bluetoothRepository.startScan()
        }
    }

    /**
     * Stop the current BLE scan without connecting.
     * Called when user taps "Stop Scanning" or navigates away.
     */
    fun stopScan() {
        bluetoothRepository.stopScan()
    }

    /**
     * Connect to a specific device from the discovered list.
     * Called when user taps a device card.
     */
    fun connect(device: BleDevice) {
        viewModelScope.launch {
            bluetoothRepository.connect(device)
        }
    }

    /**
     * Disconnect from the current device.
     * Called when user taps "Disconnect".
     */
    fun disconnect() {
        bluetoothRepository.disconnect()
    }

    /**
     * Retry after a connection failure.
     * Called when user taps "Try Again" on the error state.
     */
    fun retry() {
        bluetoothRepository.retry()
    }

    /**
     * Connect to the simulated device directly (no scan needed).
     * Useful when physical hardware isn't available.
     */
    fun connectSimulated() {
        viewModelScope.launch {
            bluetoothRepository.connectSimulated()
        }
    }

    /**
     * Update permission state after the system permission dialog resolves.
     * Call this from the UI with the result of PermissionLauncher.
     */
    fun onPermissionsResult(granted: Boolean) {
        _permissionsGranted.value = granted
        if (granted) {
            // Auto-start scan once permissions are granted
            startScan()
        }
    }

    /**
     * Called when the ViewModel is destroyed (screen permanently leaves).
     * Stops the scan to avoid battery drain.
     */
    override fun onCleared() {
        super.onCleared()
        bluetoothRepository.stopScan()
    }
}