// ============================================================
// File: ui/screens/TranslationScreen.kt
// Purpose: The core feature screen — live sign → text translation.
//
// UI LAYOUT:
//   ┌──────────────────────────────────┐
//   │ ← Back    Live Translation  ⚙️  │  ← top bar
//   │ [● LISTENING]  TTS [ON]  [PAUSE]│  ← status strip
//   ├──────────────────────────────────┤
//   │                                  │
//   │     H E L L O                    │  ← BIG latest word
//   │     ──────────                   │
//   │     High confidence  0.94        │
//   │                                  │
//   ├──────────────────────────────────┤
//   │  Full sentence:                  │
//   │  "Hello yes thank you..."        │  ← scrollable session text
//   ├──────────────────────────────────┤
//   │  Recent (tap any word to replay) │
//   │  [Hello] [Yes] [No] [Thank you]  │  ← event chips
//   ├──────────────────────────────────┤
//   │  [🔊 Read All]  [🗑 Clear]       │  ← action buttons
//   └──────────────────────────────────┘
//
// Design: Dark/focused interface. Minimal chrome.
// The translated word is THE focus — everything else supports it.
//
// Nielsen heuristics:
//   #1: Animated status indicator always visible
//   #3: Pause button gives full user control
//   #5: TTS on/off toggle prevents unwanted speech
//   #7: Flexibility — can read entire session or single words
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.navigation.Screen
import com.signlink.app.data.translation.TranslationEvent
import com.signlink.app.data.translation.TranslationStatus
import com.signlink.app.ui.theme.*
import com.signlink.app.utils.TtsState
import com.signlink.app.viewmodel.TranslationViewModel

// ── TranslationScreen ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(navController: NavHostController) {

    val viewModel: TranslationViewModel = hiltViewModel()

    val status        by viewModel.translationStatus.collectAsStateWithLifecycle()
    val isPaused      by viewModel.isPaused.collectAsStateWithLifecycle()
    val sessionText   by viewModel.sessionText.collectAsStateWithLifecycle()
    val eventHistory  by viewModel.eventHistory.collectAsStateWithLifecycle()
    val ttsEnabled    by viewModel.ttsEnabled.collectAsStateWithLifecycle()
    val ttsState      by viewModel.ttsState.collectAsStateWithLifecycle()
    val latestWord    by viewModel.latestWord.collectAsStateWithLifecycle()

    // Clear dialog state
    var showClearDialog by remember { mutableStateOf(false) }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title  = { Text("Clear session?") },
            text   = { Text("This will remove all translated text from this session. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearSession()
                    showClearDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text  = "Live Translation",
                            style = MaterialTheme.typography.titleMedium.copy(
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
                        // TTS toggle button
                        IconButton(onClick = { viewModel.toggleTts() }) {
                            Icon(
                                imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp
                                else            Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = if (ttsEnabled) "Mute TTS" else "Enable TTS",
                                tint = if (ttsEnabled) MaterialTheme.colorScheme.primary
                                else            MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Filled.Settings, "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                // ── Status strip ──────────────────────────────
                StatusStrip(
                    status     = status,
                    isPaused   = isPaused,
                    ttsEnabled = ttsEnabled,
                    ttsState   = ttsState,
                    onTogglePause = { viewModel.togglePause() }
                )
            }
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
                    .size(300.dp)
                    .offset(x = (-150).dp, y = (-50).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )

            LazyColumn(
                modifier        = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding  = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Big latest-word display ────────────────────────
                item {
                    LatestWordDisplay(
                        word      = latestWord,
                        isPaused  = isPaused,
                        eventHistory = eventHistory
                    )
                }

                // ── Full session text card ─────────────────────────
                if (sessionText.isNotBlank()) {
                    item {
                        SessionTextCard(
                            text    = sessionText,
                            onSpeak = { viewModel.speakSessionText() }
                        )
                    }
                }

                // ── Event history chips ────────────────────────────
                if (eventHistory.isNotEmpty()) {
                    item {
                        EventHistoryRow(
                            events   = eventHistory,
                            onSpeak  = { viewModel.speakWord(it.text) }
                        )
                    }
                }

                // ── Bottom action buttons ──────────────────────────
                item {
                    BottomActions(
                        sessionText  = sessionText,
                        onSpeakAll   = { viewModel.speakSessionText() },
                        onClear      = { showClearDialog = true }
                    )
                }

                // ── Empty state ────────────────────────────────────
                if (eventHistory.isEmpty()) {
                    item {
                        EmptyTranslationState(isPaused = isPaused)
                    }
                }

                // Bottom spacing
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ── StatusStrip ───────────────────────────────────────────────
/**
 * The status bar below the top app bar.
 * Shows: animated status dot + label, TTS indicator, pause button.
 */
@Composable
private fun StatusStrip(
    status:       TranslationStatus,
    isPaused:     Boolean,
    ttsEnabled:   Boolean,
    ttsState:     TtsState,
    onTogglePause: () -> Unit
) {
    // Pulsing animation for the LISTENING dot
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.3f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (statusColor, statusLabel) = when (status) {
        TranslationStatus.LISTENING    -> SignLinkTheme.colors.success to "LISTENING"
        TranslationStatus.PROCESSING   -> SignLinkConnecting to "PROCESSING"
        TranslationStatus.PAUSED       -> SignLinkConnecting to "PAUSED"
        TranslationStatus.DISCONNECTED -> SignLinkTheme.colors.error to "DISCONNECTED"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: status dot + label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsing status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            statusColor.copy(
                                alpha = if (status == TranslationStatus.LISTENING) pulseAlpha else 1f
                            )
                        )
                )
                Text(
                    text  = statusLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = statusColor
                )
            }

            // Right: TTS label + Pause button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TTS speaking indicator
                if (ttsState == TtsState.SPEAKING) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text  = "Speaking",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Pause / Resume button
                FilledTonalButton(
                    onClick      = onTogglePause,
                    modifier     = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors       = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isPaused)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Filled.PlayArrow
                        else          Icons.Filled.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = if (isPaused) "Resume" else "Pause",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ── LatestWordDisplay ─────────────────────────────────────────
/**
 * The BIG translated word in the center of the screen.
 * Animates in with a slide-up when a new word arrives.
 */
@Composable
private fun LatestWordDisplay(
    word:         String,
    isPaused:     Boolean,
    eventHistory: List<TranslationEvent>
) {
    val latestEvent = eventHistory.firstOrNull()

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 180.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 180.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (word.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Animated word entrance
                    AnimatedContent(
                        targetState   = word,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()).togetherWith(
                                slideOutVertically { -it } + fadeOut()
                            )
                        },
                        label = "wordAnim"
                    ) { displayWord ->
                        Text(
                            text      = displayWord,
                            style     = MaterialTheme.typography.displaySmall.copy(
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color     = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Confidence indicator
                    latestEvent?.let { event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Confidence color dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (event.confidenceLabel) {
                                            "High"   -> SignLinkTheme.colors.success
                                            "Medium" -> SignLinkConnecting
                                            else     -> SignLinkTheme.colors.error
                                        }
                                    )
                            )
                            Text(
                                text  = "${event.confidenceLabel} confidence · ${(event.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // No translation yet
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = if (isPaused) "⏸" else "🤟",
                        fontSize = 48.sp
                    )
                    Text(
                        text  = if (isPaused) "Translation paused"
                        else          "Waiting for gesture...",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── SessionTextCard ───────────────────────────────────────────
/**
 * Shows the full session text (all translated words joined).
 */
@Composable
private fun SessionTextCard(
    text:    String,
    onSpeak: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Session text",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Speak-all mini button
                IconButton(
                    onClick  = onSpeak,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.RecordVoiceOver, "Read aloud",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text      = text,
                style     = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp
                ),
                color     = MaterialTheme.colorScheme.onSurface,
                maxLines  = 6,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}

// ── EventHistoryRow ───────────────────────────────────────────
/**
 * Horizontal scrolling row of recent gesture chips.
 * Tap any chip to re-speak that word.
 */
@Composable
private fun EventHistoryRow(
    events:  List<TranslationEvent>,
    onSpeak: (TranslationEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = "Recent gestures — tap to replay",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            events.take(10).forEachIndexed { index, event ->
                val isLatest = index == 0
                Surface(
                    onClick = { onSpeak(event) },
                    shape   = RoundedCornerShape(50.dp),
                    color   = if (isLatest)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    border  = null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp, null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isLatest)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = event.text,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isLatest)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── BottomActions ─────────────────────────────────────────────
/**
 * Read All and Clear buttons at the bottom.
 */
@Composable
private fun BottomActions(
    sessionText: String,
    onSpeakAll:  () -> Unit,
    onClear:     () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Read All button
        OutlinedButton(
            onClick  = onSpeakAll,
            enabled  = sessionText.isNotBlank(),
            modifier = Modifier.weight(1f).height(48.dp),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Filled.RecordVoiceOver, null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "Read All",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Clear button
        OutlinedButton(
            onClick  = onClear,
            enabled  = sessionText.isNotBlank(),
            modifier = Modifier.weight(1f).height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border   = null
        ) {
            Icon(
                Icons.Filled.DeleteSweep, null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "Clear",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ── EmptyTranslationState ─────────────────────────────────────
@Composable
private fun EmptyTranslationState(isPaused: Boolean) {
    if (!isPaused) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text  = "How it works",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(4.dp))
                listOf(
                    "🤟" to "Make a sign language gesture with your wristband hand",
                    "⚡" to "SignLink detects the gesture in real-time",
                    "💬" to "The translation appears above and is spoken aloud",
                    "📝" to "All words are saved in Session text for review"
                ).forEach { (icon, text) ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = icon, fontSize = 16.sp)
                        Text(
                            text  = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}