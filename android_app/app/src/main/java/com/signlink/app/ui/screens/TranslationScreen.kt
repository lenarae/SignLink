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
import com.signlink.app.navigation.Screen
import com.signlink.app.data.translation.TranslationEvent
import com.signlink.app.data.translation.TranslationStatus
import com.signlink.app.ui.theme.*
import com.signlink.app.utils.TtsState
import com.signlink.app.viewmodel.TranslationViewModel

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

    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title  = { Text(stringResource(R.string.translation_clear) + "?") },
            text   = { Text(stringResource(R.string.settings_clear_confirm_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearSession()
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.translation_clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
                            text  = stringResource(R.string.translation_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleTts() }) {
                            Icon(
                                imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp
                                else            Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = null,
                                tint = if (ttsEnabled) MaterialTheme.colorScheme.primary
                                else            MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Filled.Settings, stringResource(R.string.settings))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
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
                item {
                    LatestWordDisplay(
                        word      = latestWord,
                        isPaused  = isPaused,
                        eventHistory = eventHistory
                    )
                }

                if (sessionText.isNotBlank()) {
                    item {
                        SessionTextCard(
                            text    = sessionText,
                            onSpeak = { viewModel.speakSessionText() }
                        )
                    }
                }

                if (eventHistory.isNotEmpty()) {
                    item {
                        EventHistoryRow(
                            events   = eventHistory,
                            onSpeak  = { viewModel.speakWord(it.text) }
                        )
                    }
                }

                item {
                    BottomActions(
                        sessionText  = sessionText,
                        onSpeakAll   = { viewModel.speakSessionText() },
                        onClear      = { showClearDialog = true }
                    )
                }

                if (eventHistory.isEmpty()) {
                    item {
                        EmptyTranslationState(isPaused = isPaused)
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun StatusStrip(
    status:       TranslationStatus,
    isPaused:     Boolean,
    ttsEnabled:   Boolean,
    ttsState:     TtsState,
    onTogglePause: () -> Unit
) {
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

    val (statusColor, statusLabelRes) = when (status) {
        TranslationStatus.LISTENING    -> SignLinkTheme.colors.success to R.string.status_listening
        TranslationStatus.PROCESSING   -> SignLinkConnecting to R.string.status_processing
        TranslationStatus.PAUSED       -> SignLinkConnecting to R.string.status_paused
        TranslationStatus.DISCONNECTED -> SignLinkTheme.colors.error to R.string.status_disconnected
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                    text  = stringResource(statusLabelRes),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = statusColor
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                            text  = stringResource(R.string.status_speaking),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

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
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = if (isPaused) stringResource(R.string.continue_label) else stringResource(R.string.cancel), // Simplified
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

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

                    latestEvent?.let { event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = if (isPaused) "⏸" else "🤟",
                        fontSize = 48.sp
                    )
                    Text(
                        text  = if (isPaused) stringResource(R.string.translation_paused_msg)
                        else          stringResource(R.string.translation_waiting),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

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
                    text  = stringResource(R.string.translation_session_text),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick  = onSpeak,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.RecordVoiceOver, null,
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
                color     = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EventHistoryRow(
    events:  List<TranslationEvent>,
    onSpeak: (TranslationEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = stringResource(R.string.translation_recent_gestures),
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
                text  = stringResource(R.string.translation_read_all),
                style = MaterialTheme.typography.labelLarge
            )
        }

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
                text  = stringResource(R.string.translation_clear),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

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
                    text  = stringResource(R.string.translation_how_it_works),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(4.dp))
                listOf(
                    "🤟" to R.string.translation_step1,
                    "⚡" to R.string.translation_step2,
                    "💬" to R.string.translation_step3,
                    "📝" to R.string.translation_step4
                ).forEach { (icon, textRes) ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = icon, fontSize = 16.sp)
                        Text(
                            text  = stringResource(textRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}