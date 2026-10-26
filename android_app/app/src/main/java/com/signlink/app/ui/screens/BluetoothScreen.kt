// ============================================================
// File: ui/screens/BluetoothScreen.kt
// Purpose: The complete Bluetooth management screen.
//
// This screen handles ALL Bluetooth states:
//   1. Permissions check (request BT permissions if not granted)
//   2. Scanning (animated radar + device list)
//   3. Connecting (progress indicator)
//   4. Connected (status card + disconnect button)
//   5. Failed (error message + retry)
//   6. Demo mode button (connects to simulated device instantly)
//
// Nielsen heuristics:
//   - #1: Always shows current connection status
//   - #3: Back button available in top bar
//   - #4: Consistent status colors with rest of app
//   - #9: Clear error messages with actionable retry button
// ============================================================

package com.signlink.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.data.bluetooth.BleDevice
import com.signlink.app.data.bluetooth.ConnectionState
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import com.signlink.app.viewmodel.BluetoothViewModel

// ── Permission list ───────────────────────────────────────────
// Android 12+ needs BLUETOOTH_SCAN and BLUETOOTH_CONNECT.
// Older versions need FINE_LOCATION for BLE scanning.
private val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
} else {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

// ── BluetoothScreen ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(navController: NavHostController) {

    // hiltViewModel() auto-creates and injects the ViewModel
    val viewModel: BluetoothViewModel = hiltViewModel()

    // Collect state flows as Compose State (lifecycle-aware)
    val connectionState   by viewModel.connectionState.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val isScanning        by viewModel.isScanning.collectAsStateWithLifecycle()
    val isConnected       by viewModel.isConnected.collectAsStateWithLifecycle()

    // ── Permission launcher ───────────────────────────────────
    // This shows the system permission dialog and gets the result
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.onPermissionsResult(allGranted)
    }

    // ── Auto-request permissions on first load ────────────────
    LaunchedEffect(Unit) {
        permissionLauncher.launch(BLUETOOTH_PERMISSIONS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Connect Device",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show disconnect button when connected
                    if (isConnected) {
                        TextButton(onClick = { viewModel.disconnect() }) {
                            Text(
                                text  = "Disconnect",
                                color = SignLinkDisconnected,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        // ── Route to correct state UI ─────────────────────────
        // Each connection state gets its own dedicated composable
        when (val state = connectionState) {

            is ConnectionState.Disconnected -> DisconnectedContent(
                devices        = discoveredDevices,
                isScanning     = isScanning,
                onScan         = { viewModel.startScan() },
                onStopScan     = { viewModel.stopScan() },
                onConnect      = { device -> viewModel.connect(device) },
                onDemoMode     = { viewModel.connectSimulated() },
                padding        = padding
            )

            is ConnectionState.Scanning -> DisconnectedContent(
                devices        = discoveredDevices,
                isScanning     = true,
                onScan         = { viewModel.startScan() },
                onStopScan     = { viewModel.stopScan() },
                onConnect      = { device -> viewModel.connect(device) },
                onDemoMode     = { viewModel.connectSimulated() },
                padding        = padding
            )

            is ConnectionState.Connecting -> ConnectingContent(
                deviceName = state.deviceName,
                padding    = padding
            )

            is ConnectionState.Connected -> ConnectedContent(
                device         = state.device,
                onDisconnect   = { viewModel.disconnect() },
                onGoTranslate  = { navController.navigate(Screen.Translation.route) },
                onCalibrate    = { navController.navigate(Screen.Calibration.route) },
                padding        = padding
            )

            is ConnectionState.Failed -> FailedContent(
                reason    = state.reason,
                onRetry   = { viewModel.retry() },
                onDemoMode = { viewModel.connectSimulated() },
                padding   = padding
            )
        }
    }
}

// ── DisconnectedContent ────────────────────────────────────────
/**
 * Shown when not connected. Displays the scan button and device list.
 */
@Composable
private fun DisconnectedContent(
    devices:    List<BleDevice>,
    isScanning: Boolean,
    onScan:     () -> Unit,
    onStopScan: () -> Unit,
    onConnect:  (BleDevice) -> Unit,
    onDemoMode: () -> Unit,
    padding:    PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        // ── Scan animation header ──────────────────────────────
        item {
            ScanHeader(
                isScanning = isScanning,
                onScan     = onScan,
                onStopScan = onStopScan
            )
        }

        // ── Demo Mode button (always visible) ─────────────────
        item {
            OutlinedButton(
                onClick  = onDemoMode,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                border   = androidx.compose.foundation.BorderStroke(
                    1.dp, SignLinkTeal500
                )
            ) {
                Icon(
                    Icons.Filled.PhoneAndroid,
                    contentDescription = null,
                    tint = SignLinkTeal500,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Demo Mode (no hardware needed)",
                    color = SignLinkTeal500,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // ── Device list header ─────────────────────────────────
        if (devices.isNotEmpty()) {
            item {
                Text(
                    text  = "Nearby devices (${devices.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // ── Device cards ───────────────────────────────────────
        items(
            items = devices,
            key   = { it.address }   // Stable keys for efficient recomposition
        ) { device ->
            DeviceCard(
                device    = device,
                onConnect = { onConnect(device) }
            )
        }

        // ── Empty state ────────────────────────────────────────
        if (devices.isEmpty() && !isScanning) {
            item {
                EmptyDeviceState()
            }
        }

        // ── Scanning indicator ─────────────────────────────────
        if (isScanning && devices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "Searching nearby...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── ScanHeader ────────────────────────────────────────────────
/**
 * Animated pulse ring + scan button at the top.
 */
@Composable
private fun ScanHeader(
    isScanning: Boolean,
    onScan:     () -> Unit,
    onStopScan: () -> Unit
) {
    // Pulsing animation for the scan ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.7f,
        targetValue   = 0.1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Bluetooth icon with pulse ring
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer pulse ring (only visible while scanning)
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(SignLinkTeal500.copy(alpha = pulseAlpha))
                    )
                }
                // Inner icon circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isScanning) SignLinkTeal500
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bluetooth,
                        contentDescription = null,
                        tint = if (isScanning) Color.White
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Status text
            Text(
                text  = if (isScanning) "Scanning for devices..."
                else "Ready to scan",
                style = MaterialTheme.typography.titleSmall,
                color = if (isScanning) SignLinkTeal500
                else MaterialTheme.colorScheme.onSurface
            )

            // Scan / Stop button
            Button(
                onClick  = if (isScanning) onStopScan else onScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) SignLinkConnecting
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = SignLinkTeal900,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Stop Scanning",
                        color = SignLinkTeal900,
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Scan for Devices",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// ── DeviceCard ────────────────────────────────────────────────
/**
 * A single discovered device shown in the scan results list.
 * SignLink devices are highlighted differently from generic devices.
 */
@Composable
private fun DeviceCard(
    device:    BleDevice,
    onConnect: () -> Unit
) {
    val isSignLink = device.isSignLinkDevice
    val borderColor = if (isSignLink) SignLinkTeal500 else Color.Transparent

    Card(
        onClick   = onConnect,
        modifier  = Modifier
            .fillMaxWidth()
            .then(
                if (isSignLink) Modifier.border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSignLink)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Device type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSignLink) SignLinkTeal500.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSignLink) Icons.Filled.Watch
                    else Icons.Filled.Bluetooth,
                    contentDescription = null,
                    tint = if (isSignLink) SignLinkTeal500
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Device info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text  = device.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // "Recommended" badge for SignLink devices
                    if (isSignLink) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SignLinkTeal500.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text     = "SignLink",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = SignLinkTeal500
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = "·",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text  = device.signalLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (device.signalLabel) {
                            "Excellent", "Good" -> SignLinkConnected
                            "Fair"              -> SignLinkConnecting
                            else                -> SignLinkDisconnected
                        }
                    )
                }
                // Signal strength bar
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress   = { device.signalStrength },
                    modifier   = Modifier
                        .fillMaxWidth(0.6f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color      = when (device.signalLabel) {
                        "Excellent", "Good" -> SignLinkConnected
                        "Fair"              -> SignLinkConnecting
                        else                -> SignLinkDisconnected
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Connect arrow
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Connect",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── ConnectingContent ─────────────────────────────────────────
/**
 * Full-screen loading state while establishing BLE connection.
 */
@Composable
private fun ConnectingContent(
    deviceName: String,
    padding:    PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(
                modifier    = Modifier.size(64.dp),
                color       = SignLinkTeal500,
                strokeWidth = 4.dp
            )
            Text(
                text  = "Connecting to",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = deviceName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = "This may take a few seconds...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── ConnectedContent ──────────────────────────────────────────
/**
 * Shown when successfully connected. Provides quick-action buttons.
 */
@Composable
private fun ConnectedContent(
    device:       BleDevice,
    onDisconnect: () -> Unit,
    onGoTranslate: () -> Unit,
    onCalibrate:  () -> Unit,
    padding:      PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Success card ──────────────────────────────────────
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(
                containerColor = SignLinkConnected.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Green check icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SignLinkConnected.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = SignLinkConnected,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(
                    text  = "Connected!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = SignLinkConnected
                )
                Text(
                    text      = device.displayName,
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                // Device details
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DeviceInfoChip(label = "Signal", value = device.signalLabel)
                    DeviceInfoChip(label = "RSSI",   value = "${device.rssi} dBm")
                    if (device.isSimulated) {
                        DeviceInfoChip(label = "Mode", value = "Simulated")
                    }
                }
            }
        }

        Text(
            text  = "What would you like to do?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── Quick action buttons ──────────────────────────────
        Button(
            onClick  = onGoTranslate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.SignLanguage, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "Start Translating",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }

        OutlinedButton(
            onClick  = onCalibrate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Tune, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "Calibrate Device",
                style = MaterialTheme.typography.labelLarge
            )
        }

        TextButton(
            onClick  = onDisconnect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text  = "Disconnect",
                color = SignLinkDisconnected,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ── FailedContent ─────────────────────────────────────────────
/**
 * Shows a clear error message with retry options.
 */
@Composable
private fun FailedContent(
    reason:    String,
    onRetry:   () -> Unit,
    onDemoMode: () -> Unit,
    padding:   PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SignLinkDisconnected.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.BluetoothDisabled,
                contentDescription = null,
                tint = SignLinkDisconnected,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text  = "Connection Failed",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text      = reason,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick  = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Try Again", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick  = onDemoMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Use Demo Mode Instead",
                style = MaterialTheme.typography.labelLarge,
                color = SignLinkTeal500
            )
        }
    }
}

// ── EmptyDeviceState ──────────────────────────────────────────
@Composable
private fun EmptyDeviceState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "📡", fontSize = 40.sp)
            Text(
                text      = "No devices found yet",
                style     = MaterialTheme.typography.titleSmall,
                color     = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text      = "Make sure your wristband is powered on\nand within 10 meters",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── DeviceInfoChip ────────────────────────────────────────────
@Composable
private fun DeviceInfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}