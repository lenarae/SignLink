package com.signlink.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.data.local.ChatMessage
import com.signlink.app.data.local.MessageSource
import com.signlink.app.data.local.SessionSummary
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import com.signlink.app.viewmodel.ChatHistoryViewModel
import com.signlink.app.viewmodel.HistoryViewMode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(navController: NavHostController) {

    val viewModel: ChatHistoryViewModel = hiltViewModel()

    val viewMode            by viewModel.viewMode.collectAsStateWithLifecycle()
    val searchQuery         by viewModel.searchQuery.collectAsStateWithLifecycle()
    val displayedMessages   by viewModel.displayedMessages.collectAsStateWithLifecycle()
    val sessionList         by viewModel.sessionList.collectAsStateWithLifecycle()
    val messageCount        by viewModel.messageCount.collectAsStateWithLifecycle()
    val selectedSessionId   by viewModel.selectedSessionId.collectAsStateWithLifecycle()
    val selectedMessages    by viewModel.selectedSessionMessages.collectAsStateWithLifecycle()
    val showDeleteAllDialog by viewModel.showDeleteAllDialog.collectAsStateWithLifecycle()


    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteAllDialog() },
            icon    = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title   = { Text("Delete all history?") },
            text    = { Text("This permanently removes all $messageCount stored messages. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAllMessages() }) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteAllDialog() }) { Text("Cancel") }
            }
        )
    }

    // ── Session drill-down ────────────────────────────────────
    if (selectedSessionId != null) {
        SessionDetailScreen(
            sessionId  = selectedSessionId!!,
            messages   = selectedMessages,
            onBack     = { viewModel.clearSessionSelection() },
            onDelete   = { viewModel.deleteSession(selectedSessionId!!) },
            onDeleteMsg = { viewModel.deleteMessage(it) }
        )
        return
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Chat History",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                "$messageCount message${if (messageCount != 1) "s" else ""} stored",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        if (messageCount > 0) {
                            IconButton(onClick = { viewModel.showDeleteAllDialog() }) {
                                Icon(
                                    Icons.Filled.DeleteSweep,
                                    "Delete all",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Filled.Settings, "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // ── Search bar ────────────────────────────────
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Search messages...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Close, "Clear search", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // ── View mode toggle ──────────────────────────
                if (searchQuery.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HistoryViewMode.values().forEach { mode ->
                            val isSelected = viewMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick  = { viewModel.setViewMode(mode) },
                                label    = {
                                    Text(
                                        text  = if (mode == HistoryViewMode.ALL_MESSAGES) "All Messages"
                                        else "By Session",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = if (isSelected) ({
                                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp))
                                }) else null
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                // ── Search results ─────────────────────────────
                searchQuery.isNotEmpty() -> {
                    if (displayedMessages.isEmpty()) {
                        item { HistoryEmptyState(isSearch = true, query = searchQuery, onSeed = {}) }
                    } else {
                        item {
                            Text(
                                "${displayedMessages.size} result${if (displayedMessages.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(displayedMessages, key = { it.id }) { msg ->
                            MessageCard(message = msg, onDelete = { viewModel.deleteMessage(msg) })
                        }
                    }
                }

                // ── By session view ────────────────────────────
                viewMode == HistoryViewMode.BY_SESSION -> {
                    if (sessionList.isEmpty()) {
                        item { HistoryEmptyState(isSearch = false, query = "", onSeed = { viewModel.seedDemoData() }) }
                    } else {
                        items(sessionList, key = { it.sessionId }) { session ->
                            SessionCard(
                                session   = session,
                                onClick   = { viewModel.selectSession(session.sessionId) },
                                onDelete  = { viewModel.deleteSession(session.sessionId) }
                            )
                        }
                    }
                }

                // ── All messages view ──────────────────────────
                else -> {
                    if (displayedMessages.isEmpty()) {
                        item { HistoryEmptyState(isSearch = false, query = "", onSeed = { viewModel.seedDemoData() }) }
                    } else {
                        items(displayedMessages, key = { it.id }) { msg ->
                            MessageCard(message = msg, onDelete = { viewModel.deleteMessage(msg) })
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── MessageCard ────────────────────────────────────────────────
@Composable
private fun MessageCard(
    message:  ChatMessage,
    onDelete: () -> Unit
) {
    val timeLabel = remember(message.timestampMs) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(message.timestampMs))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Source badge + text
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Source badge
                        SourceBadge(source = message.source)

                        // Confidence for SIGN messages
                        message.confidence?.let { conf ->
                            Text(
                                text  = "${(conf * 100).toInt()}% confidence",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    conf >= 0.90f -> SignLinkTheme.colors.success
                                    conf >= 0.75f -> SignLinkConnecting
                                    else          -> SignLinkTheme.colors.error
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text     = message.text,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Delete button
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline, "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text  = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── SessionCard ────────────────────────────────────────────────
@Composable
private fun SessionCard(
    session:  SessionSummary,
    onClick:  () -> Unit,
    onDelete: () -> Unit
) {
    val timeLabel = remember(session.latest) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(session.latest))
    }
    val shortId = session.sessionId.take(8)

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Chat, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Session $shortId…",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    timeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.DeleteOutline, "Delete session", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── SessionDetailScreen ───────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDetailScreen(
    sessionId:   String,
    messages:    List<ChatMessage>,
    onBack:      () -> Unit,
    onDelete:    () -> Unit,
    onDeleteMsg: (ChatMessage) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Session", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(sessionId.take(16) + "…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.DeleteSweep, "Delete session", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageCard(message = msg, onDelete = { onDeleteMsg(msg) })
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── SourceBadge ───────────────────────────────────────────────
@Composable
private fun SourceBadge(source: MessageSource) {
    val (label, color) = when (source) {
        MessageSource.SIGN   -> "🤟 SIGN"   to SignLinkTeal500
        MessageSource.SPEECH -> "🎙️ SPEECH" to MaterialTheme.colorScheme.secondary
        MessageSource.TTS    -> "⌨️ TTS"    to MaterialTheme.colorScheme.tertiary
        MessageSource.SYSTEM -> "ℹ️ SYSTEM" to MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color    = color
        )
    }
}

// ── HistoryEmptyState ─────────────────────────────────────────
@Composable
private fun HistoryEmptyState(
    isSearch: Boolean,
    query:    String,
    onSeed:   () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (isSearch) "🔍" else "💬", fontSize = 48.sp)
        Text(
            text  = if (isSearch) "No results for \"$query\"" else "No history yet",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center
        )
        Text(
            text      = if (isSearch) "Try a different search term."
            else          "Translated gestures and speech will appear here automatically.",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (!isSearch) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onSeed, shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Filled.Science, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Load demo data", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}