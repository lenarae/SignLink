package com.signlink.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

private val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
} else {
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairDeviceScreen(navController: NavHostController) {
    val viewModel: BluetoothViewModel = hiltViewModel()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.onPermissionsResult(allGranted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(BLUETOOTH_PERMISSIONS)
    }

    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            navController.navigate(Screen.Calibration.route)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(
                        onClick = { navController.navigateUp() },
                        contentPadding = PaddingValues(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF5F6368),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Back",
                            color = Color(0xFF5F6368),
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Equal top spacing (Reduced by another 1/4)
            Spacer(Modifier.height(27.dp))

            // ── TOP: Hero Icon ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Watch,
                    contentDescription = null,
                    tint = Color(0xFF1A73E8),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── HEADER ────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Pair Your Wearable",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Connect your sEMG/IMU wristband to enable sign language translation.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF5F6368),
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── INSTRUCTIONS ──────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Before you start:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "1. Turn on your wearable device\n2. Make sure Bluetooth is enabled on your phone\n3. Keep the device within 3 feet of your phone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3C4043),
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── SCAN BUTTON ───────────────────────────────────
            Button(
                onClick = { if (isScanning) viewModel.stopScan() else viewModel.startScan() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isScanning) "Scanning..." else "Scan for Devices",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── DEVICE LIST ───────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Available Devices:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF3C4043)
                )
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (discoveredDevices.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = if (isScanning) "Searching..." else "No devices found",
                                color = Color(0xFF9AA0A6),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(discoveredDevices) { device ->
                                Card(
                                    onClick = { viewModel.connect(device) },
                                    modifier = Modifier.fillMaxWidth().height(72.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Watch,
                                            contentDescription = null,
                                            tint = Color(0xFF5F6368),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Text(
                                            text = device.displayName,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── FOOTER SKIP ───────────────────────────────────
            TextButton(
                onClick = { navController.navigate(Screen.Calibration.route) }
            ) {
                Text(
                    text = "Skip for now (limited functionality)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    color = Color(0xFF1A73E8)
                )
            }
            
            // Equal bottom spacing (Reduced by another 1/4)
            Spacer(Modifier.height(27.dp))
        }
    }
}
