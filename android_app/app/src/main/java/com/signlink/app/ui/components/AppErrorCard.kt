package com.signlink.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.signlink.app.ui.theme.SignLinkConnecting
import com.signlink.app.ui.theme.SignLinkTheme
import com.signlink.app.utils.AppError
import com.signlink.app.utils.ErrorType

/**
 * A prominent card that shows an error with title, message,
 * and an optional recovery action button.
 *
 * @param error     The AppError to display
 * @param onAction  Called when the action button is tapped (null = hide button)
 * @param onDismiss Optional dismiss callback — shows X button if provided
 */
@Composable
fun AppErrorCard(
    error:     AppError,
    onAction:  (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier:  Modifier      = Modifier
) {
    val isWarning = error.type == ErrorType.BLUETOOTH_OFF ||
            error.type == ErrorType.NO_DEVICE_FOUND

    val containerColor = if (isWarning)
        SignLinkConnecting.copy(alpha = 0.10f)
    else
        SignLinkTheme.colors.error.copy(alpha = 0.08f)

    val accentColor = if (isWarning) SignLinkConnecting else SignLinkTheme.colors.error

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment    = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (error.type) {
                            ErrorType.BLUETOOTH_OFF,
                            ErrorType.BLUETOOTH_PERMISSION,
                            ErrorType.BLUETOOTH_TIMEOUT,
                            ErrorType.DEVICE_DISCONNECTED -> Icons.Filled.BluetoothDisabled
                            ErrorType.NO_DEVICE_FOUND     -> Icons.Filled.Search
                            ErrorType.MICROPHONE_PERMISSION -> Icons.Filled.MicOff
                            ErrorType.DATABASE_ERROR       -> Icons.Filled.Storage
                            ErrorType.TTS_UNAVAILABLE      -> Icons.AutoMirrored.Filled.VolumeOff
                            ErrorType.NETWORK_ERROR        -> Icons.Filled.WifiOff
                            ErrorType.UNKNOWN              -> Icons.Filled.ErrorOutline
                        },
                        contentDescription = null,
                        tint     = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Title + message
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = error.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = accentColor
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = error.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Dismiss button
                if (onDismiss != null) {
                    IconButton(
                        onClick  = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close, "Dismiss",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action button
            if (onAction != null && error.actionLabel != null) {
                Button(
                    onClick  = onAction,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor   = accentColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        error.actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

/**
 * A thin banner at the top of a screen for transient errors
 * (e.g. "Device disconnected" during translation).
 * Animates in/out automatically.
 */
@Composable
fun ErrorBanner(
    message:   String?,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible && message != null,
        enter   = slideInVertically { -it } + fadeIn(),
        exit    = slideOutVertically { -it } + fadeOut()
    ) {
        Surface(
            color         = SignLinkTheme.colors.error,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.Warning, null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text     = message ?: "",
                    style    = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color    = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Filled.Close, "Dismiss",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}