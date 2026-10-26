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
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text  = "Device Calibration",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text  = when (sessionState) {
                                    is CalibrationSessionState.NotStarted -> "Ready to begin"
                                    is CalibrationSessionState.InProgress ->
                                        "Step ${(sessionState as CalibrationSessionState.InProgress).step} of ${CALIBRATION_STEPS.size}"
                                    is CalibrationSessionState.Complete -> "Calibration complete!"
                                    is CalibrationSessionState.Failed   -> "Calibration failed"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.resetCalibration()
                            navController.navigateUp()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // ── Overall progress bar ───────────────────────
                // Thin colored bar spanning full width below the top bar
                LinearProgressIndicator(
                    progress      = { animatedOverallProgress },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color         = when (sessionState) {
                        is CalibrationSessionState.Complete -> SignLinkConnected
                        is CalibrationSessionState.Failed   -> SignLinkDisconnected
                        else                                -> SignLinkTeal500
                    },
                    trackColor    = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Route based on session state ───────────────────
            when (val state = sessionState) {

                is CalibrationSessionState.NotStarted -> {
                    ReadyToStartContent(
                        signalStrength = signalStrength,
                        onStart        = { viewModel.startCalibration() }
                    )
                }

                is CalibrationSessionState.InProgress -> {
                    InProgressContent(
                        state            = state,
                        signalStrength   = signalStrength,
                        channelQualities = channelQualities,
                        onCancel         = {
                            viewModel.resetCalibration()
                            navController.navigateUp()
                        }
                    )
                }

                is CalibrationSessionState.Complete -> {
                    CompleteContent(
                        onGoTranslate = {
                            navController.navigate(Screen.Translation.route) {
                                popUpTo(Screen.Calibration.route) { inclusive = true }
                            }
                        },
                        onRecalibrate = { viewModel.resetCalibration() }
                    )
                }

                is CalibrationSessionState.Failed -> {
                    FailedCalibrationContent(
                        reason  = state.reason,
                        onRetry = { viewModel.retryCalibration() },
                        onBack  = {
                            viewModel.resetCalibration()
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}

// ── ReadyToStartContent ────────────────────────────────────────
/**
 * Pre-calibration screen showing instructions + Start button.
 */
@Composable
private fun ReadyToStartContent(
    signalStrength: Float,
    onStart:        () -> Unit
) {
    // Hero instruction card
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big wristband icon
            Text(text = "🤟", fontSize = 56.sp)

            Text(
                text      = "Before we begin",
                style     = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text      = "Calibration takes about 15 seconds.\nMake sure your wristband is:\n\n" +
                        "• Snugly fitted — not too tight, not loose\n" +
                        "• Electrodes touching your skin\n" +
                        "• LEDs facing downward (toward your wrist)",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }

    // Live signal preview card
    SignalMeterCard(
        signalStrength = signalStrength,
        label          = "Current signal"
    )

    // Step preview list
    StepPreviewList()

    Spacer(Modifier.height(8.dp))

    // Start button
    Button(
        onClick  = onStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text  = "Begin Calibration",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
        )
    }
}

// ── InProgressContent ─────────────────────────────────────────
/**
 * Active calibration view: current step card + signal + step list.
 */
@Composable
private fun InProgressContent(
    state:            CalibrationSessionState.InProgress,
    signalStrength:   Float,
    channelQualities: List<Float>,
    onCancel:         () -> Unit
) {
    val currentStepData = CALIBRATION_STEPS.getOrNull(state.step - 1)
        ?: return

    // ── Current step hero card ─────────────────────────────────
    ActiveStepCard(
        step      = currentStepData,
        stepState = state.stepState
    )

    // ── Signal meter ───────────────────────────────────────────
    SignalMeterCard(
        signalStrength = signalStrength,
        label          = "EMG signal strength"
    )

    // ── Channel quality grid (shown during Step 2) ────────────
    if (state.step == 2) {
        ChannelQualityGrid(channelQualities = channelQualities)
    }

    // ── Step checklist ─────────────────────────────────────────
    StepChecklist(currentStep = state.step, stepState = state.stepState)

    Spacer(Modifier.height(4.dp))

    // Cancel button (always available — Nielsen #3: user control)
    TextButton(
        onClick  = onCancel,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text  = "Cancel Calibration",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelLarge
        )
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
    onGoTranslate: () -> Unit,
    onRecalibrate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // Success animation
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SignLinkConnected.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.CheckCircle, null,
                tint = SignLinkConnected,
                modifier = Modifier.size(60.dp)
            )
        }
        Text(
            text  = "Calibration Complete!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SignLinkConnected,
            textAlign = TextAlign.Center
        )
        Text(
            text      = "Your wristband is now calibrated to your hand.\nYou're ready to start translating sign language.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick  = onGoTranslate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = SignLinkConnected)
        ) {
            Icon(Icons.Filled.SignLanguage, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "Start Translating",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }
        TextButton(onClick = onRecalibrate) {
            Text(
                text  = "Recalibrate",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
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
        Text(
            text      = "Calibration Failed",
            style     = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color     = SignLinkDisconnected,
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                textAlign = TextAlign.Start
            )
        }
        Button(
            onClick  = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Try Again", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onBack) {
            Text(
                text  = "Go Back",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}