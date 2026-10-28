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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.R
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
        FeatureTile(stringResource(R.string.pair_device_title), Icons.Filled.Bluetooth, stringResource(R.string.pair_device_scan), Screen.Bluetooth.route, true),
        FeatureTile(stringResource(R.string.feature_calibrate), Icons.Filled.Tune, stringResource(R.string.feature_calibrate_desc), Screen.Calibration.route, isConnected),
        FeatureTile(stringResource(R.string.feature_translate), Icons.Filled.SignLanguage, stringResource(R.string.feature_translate_desc), Screen.Translation.route, isConnected),
        FeatureTile(stringResource(R.string.feature_speech), Icons.Filled.Mic, stringResource(R.string.feature_speech_desc), Screen.Speech.route, true),
        FeatureTile(stringResource(R.string.feature_text_to_speech), Icons.AutoMirrored.Filled.VolumeUp, stringResource(R.string.feature_text_to_speech_desc), Screen.TextToSpeech.route, true),
        FeatureTile(stringResource(R.string.feature_history), Icons.Filled.History, stringResource(R.string.feature_history_desc), Screen.ChatHistory.route, true),
        FeatureTile(stringResource(R.string.feature_learn), Icons.Filled.School, stringResource(R.string.feature_learn_desc), Screen.Learning.route, true)
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
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Filled.Settings, stringResource(R.string.settings))
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
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ConnectionStatusCard(
                    connectionState = connectionState,
                    onTap = { navController.navigate(Screen.Bluetooth.route) }
                )

                Text(
                    stringResource(R.string.home_features),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )

                features.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { feature ->
                            FeatureTileCard(
                                feature = feature,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { if (feature.enabled) navController.navigate(feature.route) }
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
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
                    stringResource(R.string.home_wristband_status),
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
        shape = RoundedCornerShape(16.dp),
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
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = opacity * 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        feature.icon,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = opacity),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    feature.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacity)
                )
                Text(
                    feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = opacity),
                    lineHeight = 16.sp
                )
            }
            if (!feature.enabled) {
                Text(
                    text = stringResource(R.string.home_connect_device),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.End
                    ),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }
        }
    }
}
