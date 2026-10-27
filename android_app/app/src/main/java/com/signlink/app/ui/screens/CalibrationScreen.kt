// ============================================================
// File: ui/screens/CalibrationScreen.kt
// Purpose: 4-step device calibration wizard.
//
// UI structure:
//   ┌─────────────────────────────────┐
//   │  Top bar: Step X of 4  [══════] │  ← overall progress bar
//   ├─────────────────────────────────┤
//   │  Signal strength meter          │  ← live EMG signal bar
//   │  ┌────────────────────────────┐ │
//   │  │  Step card (icon + title)  │ │  ← current step card
//   │  │  Status message            │ │
//   │  │  Step progress bar         │ │
//   │  └────────────────────────────┘ │
//   │  Step list (all 4 steps)        │  ← step checklist below
//   │  [Cancel]          [Start/Next] │  ← bottom CTAs
//   └─────────────────────────────────┘
//
// Nielsen heuristics:
//   #1: Overall progress bar + step progress = always know where you are
//   #3: Cancel button available throughout
//   #5: Start disabled if device not connected
//   #9: Clear failure state with explanation + retry
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// ── CalibrationScreen ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(navController: NavHostController) {

    val viewModel: CalibrationViewModel = hiltViewModel()

    val sessionState     by viewModel.sessionState.collectAsStateWithLifecycle()
    val signalStrength   by viewModel.signalStrength.collectAsStateWithLifecycle()
    val channelQualities by viewModel.channelQualities.collectAsStateWithLifecycle()
    val overallProgress  by viewModel.overallProgress.collectAsStateWithLifecycle()

    // Smooth animated overall progress for the top bar
    val animatedOverallProgress by animateFloatAsState(
        targetValue   = overallProgress,
        animationSpec = tween(300),
        label         = "overallProgress"
    )

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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Route based on session state ───────────────────
            when (val state = sessionState) {

                is CalibrationSessionState.NotStarted -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ReadyToStartContent(
                            onStart = { viewModel.startCalibration() },
                            onSkip = {
                                if (navController.currentBackStackEntry?.destination?.route == Screen.Calibration.route) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }

                is CalibrationSessionState.InProgress -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InProgressContent(
                            state = state,
                            onCancel = {
                                viewModel.resetCalibration()
                                navController.navigateUp()
                            },
                            onSkip = {
                                if (navController.currentBackStackEntry?.destination?.route == Screen.Calibration.route) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }

                is CalibrationSessionState.Complete -> {
                    CompleteContent(
                        onGoHome = {
                            if (navController.currentBackStackEntry?.destination?.route == Screen.Calibration.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                is CalibrationSessionState.Failed -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
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
}

// ── ReadyToStartContent ────────────────────────────────────────
/**
 * Pre-calibration screen matching the onboarding design.
 */
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
        // ... (hero icon etc)

        // Hero Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F0FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BackHand,
                contentDescription = null,
                tint = Color(0xFF1A73E8),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Device Calibration",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Follow the instructions to calibrate your wearable for accurate gesture recognition.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF5F6368),
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Gesture steps list - Wrapped in a weighted column to fill space but stay fixed
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            CALIBRATION_STEPS.forEach { step ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circle number
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(1.dp, Color(0xFFDADCE0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step.number.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF5F6368)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Tip Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F0FE))
                .border(1.dp, Color(0xFFD2E3FC), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Tip: Hold each gesture steady for 3 seconds. The device will vibrate when complete.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1967D2),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        // Start Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
            ) {
                Text(
                    text = "Start Calibration",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            TextButton(
                onClick = onSkip
            ) {
                Text(
                    text = "Skip for now (demo mode)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    color = Color(0xFF1A73E8)
                )
            }
        }
    }
}

// ── InProgressContent ─────────────────────────────────────────
/**
 * Active calibration view matching the step-by-step design.
 */
@Composable
private fun InProgressContent(
    state:    CalibrationSessionState.InProgress,
    onCancel: () -> Unit,
    onSkip:   () -> Unit
) {
    val currentStepData = CALIBRATION_STEPS.getOrNull(state.step - 1)
        ?: return


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F0FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BackHand,
                contentDescription = null,
                tint = Color(0xFF1A73E8),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Device Calibration",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Step ${state.step} of ${CALIBRATION_STEPS.size}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5F6368)
            )
        }

        // Large Countdown Circle - Fixed size to prevent shifting
        val remainingSeconds = when (val stepState = state.stepState) {
            is CalibrationStepState.Running -> {
                val totalSec = currentStepData.durationMs / 1000
                val elapsedSec = (stepState.progress * totalSec).toInt()
                (totalSec - elapsedSec).coerceAtLeast(1)
            }
            is CalibrationStepState.Done -> 0
            else -> 3
        }

        Box(
            modifier = Modifier
                .size(130.dp)
                .border(2.dp, Color(0xFF1A73E8), CircleShape)
                .background(Color(0xFFE8F0FE).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = remainingSeconds.toString(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A73E8)
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp).height(100.dp), // Fixed height container
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentStepData.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = currentStepData.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF5F6368),
                lineHeight = 22.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Progress Bar
            val progressValue = if (state.stepState is CalibrationStepState.Running) {
                (state.stepState as CalibrationStepState.Running).progress
            } else if (state.stepState is CalibrationStepState.Done) 1f else 0f

            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.Black,
                trackColor = Color(0xFFE5E7EB)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "Hold the gesture steady...",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5F6368)
            )
        }

        // Step Dot Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CALIBRATION_STEPS.forEach { step ->
                val isCompleted = step.number < state.step
                val isCurrent = step.number == state.step
                
                val dotColor = when {
                    isCompleted -> Color(0xFF34A853) // Green for complete
                    isCurrent -> Color(0xFF1A73E8)   // Blue for current
                    else -> Color(0xFFDADCE0)        // Gray for upcoming
                }
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel", 
                    color = Color(0xFFD93025), 
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip (Demo)", 
                    color = Color(0xFF1A73E8), 
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                )
            }
        }
    }
}

// ── ActiveStepCard ────────────────────────────────────────────
/**
 * The main "current step" card with animated progress bar.
 */
@Composable
private fun ActiveStepCard(
    step:      CalibrationStep,
    stepState: CalibrationStepState
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = when (stepState) {
            is CalibrationStepState.Running -> stepState.progress
            is CalibrationStepState.Done    -> 1f
            else -> 0f
        },
        animationSpec = tween(200),
        label         = "stepProgress"
    )

    val isDone = stepState is CalibrationStepState.Done
    val statusMessage = when (stepState) {
        is CalibrationStepState.Running -> stepState.message
        is CalibrationStepState.Done    -> "✓ Complete"
        is CalibrationStepState.Waiting -> "Waiting..."
        is CalibrationStepState.Error   -> stepState.message
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isDone)
                SignLinkConnected.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Step icon in circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) SignLinkConnected.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            Icons.Filled.Check, null,
                            tint     = SignLinkConnected,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(text = step.icon, fontSize = 26.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = "Step ${step.number}: ${step.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isDone) SignLinkConnected
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDone) SignLinkConnected
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Step instruction text
            Text(
                text  = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Animated step progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDone) SignLinkConnected
                        else MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress      = { animatedProgress },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color         = if (isDone) SignLinkConnected
                    else MaterialTheme.colorScheme.primary,
                    trackColor    = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

// ── SignalMeterCard ───────────────────────────────────────────
/**
 * Live EMG signal strength meter with animated color bar.
 */
@Composable
private fun SignalMeterCard(
    signalStrength: Float,
    label:          String
) {
    val animatedStrength by animateFloatAsState(
        targetValue   = signalStrength,
        animationSpec = tween(150),
        label         = "signal"
    )

    val barColor = when {
        animatedStrength > 0.7f -> SignLinkConnected
        animatedStrength > 0.4f -> SignLinkConnecting
        animatedStrength > 0.1f -> SignLinkDisconnected
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val qualityLabel = when {
        animatedStrength > 0.7f -> "Excellent"
        animatedStrength > 0.4f -> "Good"
        animatedStrength > 0.1f -> "Weak"
        else -> "No signal"
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.GraphicEq, null,
                        tint = barColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text  = qualityLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = barColor
                )
            }
            LinearProgressIndicator(
                progress   = { animatedStrength },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// ── ChannelQualityGrid ────────────────────────────────────────
/**
 * 8-channel EMG quality grid shown during Step 2.
 * Each cell represents one EMG electrode.
 */
@Composable
private fun ChannelQualityGrid(channelQualities: List<Float>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text  = "Electrode channels",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // 4 columns × 2 rows grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                channelQualities.chunked(4).forEach { rowQuals ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowQuals.forEachIndexed { i, quality ->
                            val animQ by animateFloatAsState(
                                targetValue   = quality,
                                animationSpec = tween(300),
                                label         = "ch$i"
                            )
                            val color = when {
                                animQ > 0.7f -> SignLinkConnected
                                animQ > 0.3f -> SignLinkConnecting
                                animQ > 0f   -> SignLinkDisconnected
                                else         -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text  = "Ch${i + 1 + (if (rowQuals === channelQualities.chunked(4).first()) 0 else 4)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── StepPreviewList / StepChecklist ──────────────────────────
/**
 * Before calibration: shows all steps as a preview list.
 */
@Composable
private fun StepPreviewList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = "What we'll do:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CALIBRATION_STEPS.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = step.icon, fontSize = 20.sp)
                Column {
                    Text(
                        text  = "Step ${step.number}: ${step.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text  = "~${step.durationMs / 1000}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * During calibration: step checklist showing done/active/waiting.
 */
@Composable
private fun StepChecklist(
    currentStep: Int,
    stepState:   CalibrationStepState
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = "Progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CALIBRATION_STEPS.forEach { step ->
                val isDone    = step.number < currentStep ||
                        (step.number == currentStep && stepState is CalibrationStepState.Done)
                val isActive  = step.number == currentStep && stepState !is CalibrationStepState.Done
                val isWaiting = step.number > currentStep

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Step status indicator
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone   -> SignLinkConnected.copy(alpha = 0.15f)
                                    isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else     -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isDone   -> Icon(Icons.Filled.Check, null,
                                tint = SignLinkConnected, modifier = Modifier.size(16.dp))
                            isActive -> CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                color       = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            else     -> Text(
                                text  = "${step.number}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text  = step.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = when {
                            isDone   -> SignLinkConnected
                            isActive -> MaterialTheme.colorScheme.onSurface
                            else     -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

// ── CompleteContent ───────────────────────────────────────────
@Composable
private fun CompleteContent(
    onGoHome: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    
    // Animate between standard green and a lighter, more vibrant green
    val color1 by infiniteTransition.animateColor(
        initialValue = SignLinkConnected,
        targetValue  = Color(0xFF00C853), // Standard Green A700
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color1"
    )
    
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFFB9F6CA), // Light Green A100
        targetValue  = Color(0xFFA7FFEB), // Light Teal/Green
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(color1, color2)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing effect around the icon
                Box(contentAlignment = Alignment.Center) {
                    // Animated pulse rings
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue  = 1.6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ), label = "pulse"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle, null,
                            tint = Color(0xFF004D40), // Deep green for contrast
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(Modifier.height(21.dp))

                Text(
                    text  = "Calibration Complete!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF004D40),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = "Your device is now calibrated.\nYou're ready to use SignLink!",
                    style     = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color     = Color(0xFF004D40).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick  = onGoHome,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF004D40),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text  = "Go to Dashboard",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ── FailedCalibrationContent ──────────────────────────────────
@Composable
private fun FailedCalibrationContent(
    reason:  String,
    onRetry: () -> Unit,
    onBack:  () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SignLinkDisconnected.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ErrorOutline, null,
                tint = SignLinkDisconnected,
                modifier = Modifier.size(52.dp)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text      = "Calibration Failed",
                style     = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color     = SignLinkDisconnected,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = SignLinkDisconnected.copy(alpha = 0.08f)
                )
            ) {
                Text(
                    text      = reason,
                    modifier  = Modifier.padding(16.dp),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick  = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
            ) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
            TextButton(onClick = onBack) {
                Text(
                    text  = "Go Back",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}