// ============================================================
// File: ui/screens/CalibrationScreen.kt
// Purpose: 4-step device calibration wizard.
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.data.bluetooth.*
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import com.signlink.app.viewmodel.CalibrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(navController: NavHostController) {
    val viewModel: CalibrationViewModel = hiltViewModel()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (sessionState !is CalibrationSessionState.Complete) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        TextButton(onClick = {
                            viewModel.resetCalibration()
                            navController.navigateUp()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Back",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Filled.Settings, "Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp) // Added vertical padding
        ) {
            when (val state = sessionState) {
                is CalibrationSessionState.NotStarted -> {
                    ReadyToStartContent(
                        onStart = { viewModel.startCalibration() },
                        onSkip = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }
                is CalibrationSessionState.InProgress -> {
                    InProgressContent(
                        state = state,
                        onCancel = {
                            viewModel.resetCalibration()
                            navController.navigateUp()
                        },
                        onSkip = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }
                is CalibrationSessionState.Complete -> {
                    CompleteContent(
                        onGoHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }
                is CalibrationSessionState.Failed -> {
                    FailedCalibrationContent(
                        reason = state.reason,
                        onRetry = { viewModel.retryCalibration() },
                        onBack = {
                            viewModel.resetCalibration()
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadyToStartContent(
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // ── TOP: Header ───────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BackHand,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Device Calibration",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Follow the instructions to calibrate your wearable for accurate gesture recognition.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }

        // ── MIDDLE: Gesture steps list ─────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CALIBRATION_STEPS.forEach { step ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step.number.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // ── BOTTOM: Tip & Buttons ──────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Tip: Hold each gesture steady for 3 seconds. The device vibrates when complete.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Start Calibration", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
            TextButton(onClick = onSkip) {
                Text(text = "Skip for now (demo mode)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun InProgressContent(
    state: CalibrationSessionState.InProgress,
    onCancel: () -> Unit,
    onSkip: () -> Unit
) {
    val currentStepData = CALIBRATION_STEPS.getOrNull(state.step - 1) ?: return
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Hero Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.BackHand, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Device Calibration", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text(text = "Step ${state.step} of ${CALIBRATION_STEPS.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Countdown Circle
        val remainingSeconds = when (val stepState = state.stepState) {
            is CalibrationStepState.Running -> {
                val totalSec = currentStepData.durationMs / 1000
                val elapsedSec = (stepState.progress * totalSec).toInt()
                (totalSec - elapsedSec).coerceAtLeast(1)
            }
            is CalibrationStepState.Done -> 0
            else -> 3
        }
        Box(modifier = Modifier.size(110.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
            Text(text = remainingSeconds.toString(), style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(text = currentStepData.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text(text = currentStepData.description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val progressValue = if (state.stepState is CalibrationStepState.Running) (state.stepState as CalibrationStepState.Running).progress else if (state.stepState is CalibrationStepState.Done) 1f else 0f
            LinearProgressIndicator(progress = { progressValue }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(text = "Hold the gesture steady...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CALIBRATION_STEPS.forEach { step ->
                val dotColor = when {
                    step.number < state.step -> SignLinkTheme.colors.success
                    step.number == state.step -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancel) { Text(text = "Cancel", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)) }
            TextButton(onClick = onSkip) { Text(text = "Skip (Demo)", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) }
        }
    }
}

@Composable
private fun CompleteContent(onGoHome: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val color1 by infiniteTransition.animateColor(initialValue = MaterialTheme.colorScheme.primary, targetValue = MaterialTheme.colorScheme.tertiary, animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "color1")
    val color2 by infiniteTransition.animateColor(initialValue = MaterialTheme.colorScheme.primaryContainer, targetValue = MaterialTheme.colorScheme.tertiaryContainer, animationSpec = infiniteRepeatable(animation = tween(4500, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "color2")

    Box(modifier = Modifier.fillMaxSize().offset(x = (-24).dp).width(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp).background(Brush.verticalGradient(colors = listOf(color1, color2))), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.6f, animationSpec = infiniteRepeatable(animation = tween(1500, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse), label = "pulse")
                    Box(modifier = Modifier.size(100.dp).scale(pulseScale).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)))
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(60.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(text = "Calibration Complete!", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(text = "Your device is now calibrated.\nYou're ready to use SignLink!", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), textAlign = TextAlign.Center)
            }
            Button(onClick = onGoHome, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary)) {
                Text(text = "Go to Dashboard", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun FailedCalibrationContent(reason: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(SignLinkTheme.colors.error.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.ErrorOutline, null, tint = SignLinkTheme.colors.error, modifier = Modifier.size(52.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Calibration Failed", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = SignLinkTheme.colors.error, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SignLinkTheme.colors.error.copy(alpha = 0.08f))) {
                Text(text = reason, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
            TextButton(onClick = onBack) {
                Text(text = "Go Back", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
