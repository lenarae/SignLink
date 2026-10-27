// ============================================================
// File: ui/screens/HomeScreen.kt  [UPDATED — Phase 3]
// Now observes real BLE connection state via BluetoothViewModel.
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.data.bluetooth.ConnectionState
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import com.signlink.app.viewmodel.BluetoothViewModel

data class FeatureTile(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val route: String,
    val enabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val bluetoothViewModel: BluetoothViewModel = hiltViewModel()
    val connectionState by bluetoothViewModel.connectionState.collectAsStateWithLifecycle()
    val isConnected = connectionState is ConnectionState.Connected

    val features = listOf(
        FeatureTile("Connect Device", Icons.Filled.Bluetooth, "Scan for your wristband", Screen.Bluetooth.route, true),
        FeatureTile("Calibrate", Icons.Filled.Tune, "Set up your device", Screen.Calibration.route, isConnected),
        FeatureTile("Translate", Icons.Filled.SignLanguage, "Sign → Text & Speech", Screen.Translation.route, isConnected),
        FeatureTile("Speech Input", Icons.Filled.Mic, "Voice to text", Screen.Speech.route, true),
        FeatureTile("Text to Speech", Icons.AutoMirrored.Filled.VolumeUp, "Type to speak", Screen.TextToSpeech.route, true),
        FeatureTile("History", Icons.Filled.History, "Past conversations", Screen.ChatHistory.route, true),
        FeatureTile("Learn Signs", Icons.Filled.School, "Practice & learn", Screen.Learning.route, true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable { 
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SignLanguage, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
                        }
                        Text("SignLink", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Filled.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isSystemInDarkTheme()) {
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        }
                    )
                )
        ) {
            // Decorative orbs
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-150).dp, y = (-50).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ConnectionStatusCard(
                    connectionState = connectionState,
                    onTap = { navController.navigate(Screen.Bluetooth.route) }
                )

                Text(
                    "Features",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )

                features.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { feature ->
                            FeatureTileCard(
                                feature = feature,
                                modifier = Modifier.weight(1f),
                                onClick = { if (feature.enabled) navController.navigate(feature.route) }
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(connectionState: ConnectionState, onTap: () -> Unit) {
    val statusColor = when (connectionState) {
        is ConnectionState.Connected -> SignLinkTheme.colors.success
        is ConnectionState.Connecting, is ConnectionState.Scanning -> SignLinkConnecting
        else -> SignLinkTheme.colors.error
    }
    val statusText = when (connectionState) {
        is ConnectionState.Connected -> "Connected — ${connectionState.device.displayName}"
        is ConnectionState.Connecting -> "Connecting..."
        is ConnectionState.Scanning -> "Scanning for devices..."
        is ConnectionState.Failed -> "Connection Failed"
        is ConnectionState.Disconnected -> "Disconnected"
    }
    val statusIcon = when (connectionState) {
        is ConnectionState.Connected -> Icons.Filled.BluetoothConnected
        is ConnectionState.Connecting, is ConnectionState.Scanning -> Icons.Filled.Bluetooth
        else -> Icons.Filled.BluetoothDisabled
    }
    val actionText = when (connectionState) {
        is ConnectionState.Connected -> "Tap to manage"
        is ConnectionState.Connecting -> "Establishing connection..."
        is ConnectionState.Scanning -> "Looking for wristband..."
        is ConnectionState.Failed -> "Tap to retry"
        is ConnectionState.Disconnected -> "Tap to connect"
    }

    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Wristband Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    statusText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
                Text(
                    actionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeatureTileCard(feature: FeatureTile, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val opacity = if (feature.enabled) 1f else 0.4f
    Card(
        onClick = onClick,
        enabled = feature.enabled,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = opacity * 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        feature.icon,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = opacity),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    feature.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacity)
                )
                Text(
                    feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = opacity),
                    lineHeight = 18.sp
                )
            }
            if (!feature.enabled) {
                Text(
                    "🔗 Connect device first",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}
