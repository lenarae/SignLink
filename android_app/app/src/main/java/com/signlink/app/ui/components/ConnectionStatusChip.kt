package com.signlink.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signlink.app.data.bluetooth.ConnectionState
import com.signlink.app.ui.theme.SignLinkConnecting
import com.signlink.app.ui.theme.SignLinkTheme

@Composable
fun ConnectionStatusChip(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState is ConnectionState.Connected
    val isConnecting = connectionState is ConnectionState.Connecting || connectionState is ConnectionState.Scanning

    val infiniteTransition = rememberInfiniteTransition(label = "chipPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (statusColor, statusText, statusIcon) = when (connectionState) {
        is ConnectionState.Connected -> Triple(
            SignLinkTheme.colors.success,
            "Connected: ${connectionState.device.displayName}",
            Icons.Filled.BluetoothConnected
        )
        is ConnectionState.Connecting -> Triple(
            SignLinkConnecting,
            "Connecting...",
            Icons.AutoMirrored.Filled.BluetoothSearching
        )
        is ConnectionState.Scanning -> Triple(
            SignLinkConnecting,
            "Scanning...",
            Icons.AutoMirrored.Filled.BluetoothSearching
        )
        is ConnectionState.Failed -> Triple(
            SignLinkTheme.colors.error,
            "Connection Failed",
            Icons.Filled.BluetoothDisabled
        )
        is ConnectionState.Disconnected -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            "Wristband Disconnected",
            Icons.Filled.BluetoothDisabled
        )
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = if (isConnected) 0.4f else 0.2f),
                shape = RoundedCornerShape(20.dp)
            ),
        color = statusColor.copy(alpha = if (isSystemInDarkTheme()) 0.15f else 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        statusColor.copy(
                            alpha = if (isConnected || isConnecting) pulseAlpha else 1f
                        )
                    )
            )
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = statusColor
            )
        }
    }
}
