package com.signlink.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import com.signlink.app.utils.SpeechState
import com.signlink.app.utils.TtsState
import com.signlink.app.viewmodel.SpeechViewModel
import com.signlink.app.viewmodel.TranscriptEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechScreen(navController: NavHostController) {

    val viewModel: SpeechViewModel = hiltViewModel()

    val speechState  by viewModel.speechState.collectAsStateWithLifecycle()
    val isListening  by viewModel.isListening.collectAsStateWithLifecycle()
    val partialText  by viewModel.partialText.collectAsStateWithLifecycle()
    val history      by viewModel.history.collectAsStateWithLifecycle()
    val sessionText  by viewModel.sessionText.collectAsStateWithLifecycle()
    val ttsState     by viewModel.ttsState.collectAsStateWithLifecycle()
    val autoSpeak    by viewModel.autoSpeak.collectAsStateWithLifecycle()

    // ── Microphone permission ─────────────────────────────────
    var micPermissionGranted by remember { mutableStateOf(false) }
    var micPermissionDenied  by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micPermissionGranted = granted
        micPermissionDenied  = !granted
        if (granted) viewModel.toggleListening()
    }

    // Clear history dialog
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title   = { Text("Clear transcript?") },
            text    = { Text("All recognised speech from this session will be removed.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); showClearDialog = false }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Speech Input",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Auto-speak toggle
                    IconButton(onClick = { viewModel.toggleAutoSpeak() }) {
                        Icon(
                            imageVector = if (autoSpeak) Icons.Filled.RecordVoiceOver
                            else           Icons.Filled.VoiceOverOff,
                            contentDescription = "Auto-speak toggle",
                            tint = if (autoSpeak) MaterialTheme.colorScheme.primary
                            else           MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Filled.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
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
                    .size(250.dp)
                    .offset(x = (-100).dp, y = 50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )

            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(padding),
                contentPadding  = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            // ── Mic button section ────────────────────────────
            item {
                MicSection(
                    isListening          = isListening,
                    speechState          = speechState,
                    ttsState             = ttsState,
                    micPermissionDenied  = micPermissionDenied,
                    onMicTap = {
                        when {
                            // TTS is speaking — stop it first
                            ttsState == TtsState.SPEAKING -> viewModel.stopSpeaking()
                            // Need to request mic permission
                            !micPermissionGranted && !viewModel.isAvailable ->
                                viewModel.toggleListening() // goes to mock
                            !micPermissionGranted ->
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else -> viewModel.toggleListening()
                        }
                    }
                )
            }

            // ── Live partial text display ─────────────────────
            item {
                LiveTextDisplay(
                    speechState = speechState,
                    partialText = partialText
                )
            }

            // ── Transcript header + action buttons ────────────
            if (history.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Transcript (${history.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Speak all button
                            FilledTonalIconButton(
                                onClick  = { viewModel.speakAll() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.RecordVoiceOver,
                                    "Speak all",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Clear button
                            FilledTonalIconButton(
                                onClick  = { showClearDialog = true },
                                modifier = Modifier.size(36.dp),
                                colors   = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Icon(
                                    Icons.Filled.DeleteSweep,
                                    "Clear",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // ── History entries ───────────────────────────
                items(
                    items = history,
                    key   = { it.timestampMs }
                ) { entry ->
                    TranscriptCard(
                        entry    = entry,
                        onSpeak  = { viewModel.speakEntry(entry.text) },
                        onDelete = { viewModel.deleteEntry(entry) }
                    )
                }
            }

            // ── Empty state ───────────────────────────────────
            if (history.isEmpty() && !isListening) {
                item { SpeechEmptyState(isAvailable = viewModel.isAvailable) }
            }

            // Auto-speak status hint
            if (autoSpeak) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.RecordVoiceOver, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = "Auto-speak is ON — each result will be read aloud",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
}

// ── MicSection ────────────────────────────────────────────────
@Composable
private fun MicSection(
    isListening:         Boolean,
    speechState:         SpeechState,
    ttsState:            TtsState,
    micPermissionDenied: Boolean,
    onMicTap:            () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")

    // Three concentric pulse rings, each with different timing
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseOutCubic), RepeatMode.Restart),
        label = "p1"
    )
    val pulse1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseOutCubic), RepeatMode.Restart),
        label = "a1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1000, 300, easing = EaseOutCubic), RepeatMode.Restart),
        label = "p2"
    )
    val pulse2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1000, 300, easing = EaseOutCubic), RepeatMode.Restart),
        label = "a2"
    )

    // Button state
    val isTtsSpeaking  = ttsState == TtsState.SPEAKING
    val micColor = when {
        micPermissionDenied -> MaterialTheme.colorScheme.error
        isListening         -> SignLinkListening
        isTtsSpeaking       -> SignLinkConnecting
        else                -> MaterialTheme.colorScheme.primary
    }
    val micLabel = when {
        micPermissionDenied -> "Microphone permission denied"
        isTtsSpeaking       -> "Tap to stop speaking"
        isListening         -> "Tap to stop"
        else                -> "Tap to speak"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing rings (only while listening)
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulse1)
                        .clip(CircleShape)
                        .background(micColor.copy(alpha = pulse1Alpha))
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulse2)
                        .clip(CircleShape)
                        .background(micColor.copy(alpha = pulse2Alpha))
                )
            }

            // The mic button itself
            Surface(
                onClick        = onMicTap,
                modifier       = Modifier.size(110.dp),
                shape          = CircleShape,
                color          = micColor,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            isTtsSpeaking -> Icons.AutoMirrored.Filled.VolumeUp
                            isListening   -> Icons.Filled.Mic
                            else          -> Icons.Filled.Mic
                        },
                        contentDescription = micLabel,
                        tint     = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }

        // Status label below button
        Text(
            text      = micLabel,
            style     = MaterialTheme.typography.labelLarge,
            color     = micColor,
            textAlign = TextAlign.Center
        )
    }
}

// ── LiveTextDisplay ────────────────────────────────────────────
@Composable
private fun LiveTextDisplay(
    speechState: SpeechState,
    partialText: String
) {
    AnimatedVisibility(
        visible = speechState != SpeechState.Idle,
        enter   = fadeIn() + expandVertically(),
        exit    = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = when (speechState) {
                    is SpeechState.Error   -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    is SpeechState.NoSpeech -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    is SpeechState.Result  -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    else                   -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                }
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // State label
                val (label, labelColor) = when (speechState) {
                    is SpeechState.Listening -> "Listening..." to SignLinkListening
                    is SpeechState.Partial   -> "Transcribing..." to MaterialTheme.colorScheme.primary
                    is SpeechState.Result    -> "Done" to SignLinkTheme.colors.success
                    is SpeechState.NoSpeech  -> "No speech detected" to MaterialTheme.colorScheme.onSurfaceVariant
                    is SpeechState.Error     -> "Error" to MaterialTheme.colorScheme.error
                    else                     -> "" to MaterialTheme.colorScheme.onSurface
                }
                if (label.isNotEmpty()) {
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = labelColor
                    )
                }

                // The live text content
                val displayText = when (speechState) {
                    is SpeechState.Partial  -> speechState.text
                    is SpeechState.Result   -> speechState.text
                    is SpeechState.Error    -> speechState.message
                    is SpeechState.NoSpeech -> "Nothing was heard. Tap the mic and speak clearly."
                    is SpeechState.Listening -> "Start speaking now..."
                    else                    -> ""
                }

                if (displayText.isNotEmpty()) {
                    Text(
                        text  = displayText,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                        color = when (speechState) {
                            is SpeechState.Error -> MaterialTheme.colorScheme.error
                            else                 -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

// ── TranscriptCard ────────────────────────────────────────────
@Composable
private fun TranscriptCard(
    entry:    TranscriptEntry,
    onSpeak:  () -> Unit,
    onDelete: () -> Unit
) {
    val timeLabel = remember(entry.timestampMs) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(entry.timestampMs))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Text content
            Text(
                text  = entry.text,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Speak this entry
                    IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp, "Speak",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Delete this entry
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Close, "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── SpeechEmptyState ──────────────────────────────────────────
@Composable
private fun SpeechEmptyState(isAvailable: Boolean) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🎙️", fontSize = 40.sp)
            Text(
                text  = "No transcripts yet",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )
            Text(
                text  = if (isAvailable)
                    "Tap the microphone and speak clearly.\nYour words will appear here in real-time."
                else
                    "Speech recognition is running in demo mode.\nTap the mic to see simulated transcription.",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (!isAvailable) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text     = "Demo mode — no Google STT service detected",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}