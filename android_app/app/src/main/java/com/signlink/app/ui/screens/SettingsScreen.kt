// ============================================================
// File: ui/screens/SettingsScreen.kt
// Purpose: Complete app settings screen with live DataStore binding.
//
// SECTIONS:
//   1. Appearance    — Dark mode, High contrast, Text size
//   2. Speech        — TTS on/off, Speed slider, Pitch slider
//   3. Accessibility — Vibration toggle
//   4. Device        — Auto-connect, Show confidence %
//   5. Data & Storage — Storage on/off, Retention policy, Clear all
//   6. About         — App version, open source info
//   7. Reset         — Reset all settings to defaults
//
// UI PATTERN:
//   Each section is a Card containing SettingsItem rows.
//   Toggles use Switch, sliders use Slider, choices use
//   segmented buttons. Every change is persisted instantly.
//
// Nielsen heuristics:
//   #1: Current value always shown (switch state, slider value)
//   #3: Reset button + confirm dialog = undo support
//   #5: Destructive actions (clear data) require confirmation
//   #6: Consistent item layout throughout
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.signlink.app.data.local.TextSizeOption
import com.signlink.app.data.local.ThemeMode
import com.signlink.app.data.repository.RetentionPolicy
import com.signlink.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {

    val viewModel: SettingsViewModel = hiltViewModel()
    val settings        by viewModel.settings.collectAsStateWithLifecycle()
    val showResetDialog by viewModel.showResetDialog.collectAsStateWithLifecycle()
    val showClearDialog by viewModel.showClearDataDialog.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for feedback messages
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearFeedback()
        }
    }

    // ── Reset to defaults dialog ──────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetDialog() },
            icon    = { Icon(Icons.Filled.RestartAlt, null, tint = MaterialTheme.colorScheme.error) },
            title   = { Text("Reset all settings?") },
            text    = { Text("All preferences will return to their default values. Your chat history will not be affected.") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetToDefaults() }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideResetDialog() }) { Text("Cancel") }
            }
        )
    }

    // ── Clear data dialog ─────────────────────────────────────
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDataDialog() },
            icon    = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title   = { Text("Clear all chat history?") },
            text    = { Text("Every saved message, gesture translation and speech result will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllChatData() }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDataDialog() }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isSystemInDarkTheme()) {
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        }
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            // ── 1. APPEARANCE ──────────────────────────────────
            SettingsSection(title = "Appearance", icon = Icons.Filled.Palette) {
                ThemeSetting(
                    current = settings.theme,
                    onSelect = { viewModel.setTheme(it) }
                )
                SettingsDivider()
                TextSizeSetting(
                    currentScale = settings.textSizeScale,
                    onSelect     = { viewModel.setTextSize(it) }
                )
            }

            // ── 2. SPEECH OUTPUT (TTS) ─────────────────────────
            SettingsSection(title = "Text-to-Speech", icon = Icons.Filled.RecordVoiceOver) {

                SettingsToggleItem(
                    title    = "Auto-speak translations",
                    subtitle = "Speak each gesture translation aloud",
                    icon     = Icons.AutoMirrored.Filled.VolumeUp,
                    checked  = settings.ttsEnabled,
                    onToggle = { viewModel.setTtsEnabled(it) }
                )
                SettingsDivider()
                SettingsSliderItem(
                    title    = "Speech speed",
                    icon     = Icons.Filled.Speed,
                    value    = settings.ttsRate,
                    min      = 0.5f,
                    max      = 2.0f,
                    steps    = 5,
                    label    = formatRate(settings.ttsRate),
                    enabled  = settings.ttsEnabled,
                    onValueChange = { viewModel.setTtsRate(it) }
                )
                SettingsDivider()
                SettingsSliderItem(
                    title    = "Speech pitch",
                    icon     = Icons.Filled.GraphicEq,
                    value    = settings.ttsPitch,
                    min      = 0.5f,
                    max      = 2.0f,
                    steps    = 5,
                    label    = formatPitch(settings.ttsPitch),
                    enabled  = settings.ttsEnabled,
                    onValueChange = { viewModel.setTtsPitch(it) }
                )
            }

            // ── 3. ACCESSIBILITY ───────────────────────────────
            SettingsSection(title = "Accessibility", icon = Icons.Filled.Accessibility) {

                SettingsToggleItem(
                    title    = "Vibration feedback",
                    subtitle = "Vibrate when a gesture is detected",
                    icon     = Icons.Filled.Vibration,
                    checked  = settings.vibrationEnabled,
                    onToggle = { viewModel.setVibration(it) }
                )
            }

            // ── 4. DEVICE ──────────────────────────────────────
            SettingsSection(title = "Device", icon = Icons.Filled.Watch) {

                SettingsToggleItem(
                    title    = "Auto-reconnect",
                    subtitle = "Reconnect to last device on app start",
                    icon     = Icons.AutoMirrored.Filled.BluetoothSearching,
                    checked  = settings.autoConnect,
                    onToggle = { viewModel.setAutoConnect(it) }
                )
                SettingsDivider()
                SettingsToggleItem(
                    title    = "Show confidence score",
                    subtitle = "Display classifier confidence % on translations",
                    icon     = Icons.Filled.BarChart,
                    checked  = settings.showConfidence,
                    onToggle = { viewModel.setShowConfidence(it) }
                )
            }

            // ── 5. DATA & STORAGE ──────────────────────────────
            SettingsSection(title = "Data & Storage", icon = Icons.Filled.Storage) {

                SettingsToggleItem(
                    title    = "Save chat history",
                    subtitle = "Store translations and speech locally",
                    icon     = Icons.Filled.Save,
                    checked  = settings.storageEnabled,
                    onToggle = { viewModel.setStorageEnabled(it) }
                )
                SettingsDivider()
                RetentionPolicySetting(
                    current  = settings.retentionPolicy,
                    enabled  = settings.storageEnabled,
                    onSelect = { viewModel.setRetentionPolicy(it) }
                )
                SettingsDivider()
                SettingsActionItem(
                    title    = "Clear all chat history",
                    subtitle = "Permanently delete all stored messages",
                    icon     = Icons.Filled.DeleteForever,
                    tint     = MaterialTheme.colorScheme.error,
                    onClick  = { viewModel.showClearDataDialog() }
                )
            }

            // ── 6. ABOUT ───────────────────────────────────────
            SettingsSection(title = "About", icon = Icons.Filled.Info) {

                SettingsInfoItem(label = "App version",    value = "1.0.0")
                SettingsDivider()
                SettingsInfoItem(label = "Build",          value = "Phase 9 — Complete")
                SettingsDivider()
                SettingsInfoItem(label = "Architecture",   value = "MVVM · Jetpack Compose")
                SettingsDivider()
                SettingsInfoItem(label = "Database",       value = "RoomDB v1")
                SettingsDivider()
                SettingsInfoItem(label = "BLE Mode",       value = "Simulated (mock gestures)")
            }

            // ── 7. RESET ───────────────────────────────────────
            OutlinedButton(
                onClick  = { viewModel.showResetDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border   = null
            ) {
                Icon(Icons.Filled.RestartAlt, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reset all settings to defaults", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
}

// ═══════════════════════════════════════════════════════════════
// REUSABLE SETTINGS COMPONENTS
// ═══════════════════════════════════════════════════════════════

// ── SettingsSection ───────────────────────────────────────────
/**
 * A titled card grouping related settings items.
 */
@Composable
private fun SettingsSection(
    title:   String,
    icon:    ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Section header
        Row(
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon, null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text  = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight   = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

// ── SettingsToggleItem ────────────────────────────────────────
@Composable
private fun SettingsToggleItem(
    title:    String,
    subtitle: String? = null,
    icon:     ImageVector,
    checked:  Boolean,
    onToggle: (Boolean) -> Unit,
    enabled:  Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            icon, null,
            tint     = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else         MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else         MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            if (subtitle != null) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.4f
                    )
                )
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            enabled         = enabled,
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor  = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ── SettingsSliderItem ────────────────────────────────────────
@Composable
private fun SettingsSliderItem(
    title:        String,
    icon:         ImageVector,
    value:        Float,
    min:          Float,
    max:          Float,
    steps:        Int,
    label:        String,
    enabled:      Boolean = true,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon, null,
                tint     = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else         MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text     = title,
                style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f),
                color    = if (enabled) MaterialTheme.colorScheme.onSurface
                else         MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            // Current value label
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (enabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text     = label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color    = if (enabled)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = min..max,
            steps         = steps,
            enabled       = enabled,
            modifier      = Modifier.padding(start = 34.dp, top = 4.dp),
            colors        = SliderDefaults.colors(
                thumbColor         = MaterialTheme.colorScheme.primary,
                activeTrackColor   = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ── ThemeSetting ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSetting(
    current:  ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Palette, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text  = "Theme",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        
        val modes = ThemeMode.entries.filter { it != ThemeMode.SYSTEM }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = current == mode,
                    onClick  = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = modes.size
                    ),
                    border = SegmentedButtonDefaults.borderStroke(Color.Transparent, width = 0.dp),
                    label = {
                        Text(
                            text  = mode.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
    }
}

// ── TextSizeSetting ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextSizeSetting(
    currentScale: Float,
    onSelect:     (TextSizeOption) -> Unit
) {
    val currentOption = TextSizeOption.fromScale(currentScale)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.FormatSize, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text  = "Text size",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        val options = TextSizeOption.entries.filter { it != TextSizeOption.SMALL }
        // Segmented buttons for text size options
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = currentOption == option,
                    onClick  = { onSelect(option) },
                    shape    = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    border   = SegmentedButtonDefaults.borderStroke(Color.Transparent),
                    label = {
                        Text(
                            text  = option.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = (12 * option.scale).sp
                            )
                        )
                    }
                )
            }
        }
    }
}

// ── RetentionPolicySetting ────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RetentionPolicySetting(
    current:  RetentionPolicy,
    enabled:  Boolean,
    onSelect: (RetentionPolicy) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Schedule, null,
                tint     = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else         MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Auto-delete history",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else         MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    "How long to keep stored messages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.4f)
                )
            }
        }
        val policies = listOf(
            RetentionPolicy.FOREVER   to "Forever",
            RetentionPolicy.ONE_MONTH to "1 month",
            RetentionPolicy.ONE_DAY   to "1 day",
            RetentionPolicy.DISABLED  to "Never save"
        )
        // 2x2 grid of choice chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            policies.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (policy, label) ->
                        val isSelected = current == policy
                        FilterChip(
                            selected = isSelected,
                            enabled  = enabled,
                            onClick  = { onSelect(policy) },
                            modifier = Modifier.weight(1f),
                            border   = null,
                            label    = {
                                Text(
                                    text  = label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = if (isSelected) ({
                                Icon(
                                    Icons.Filled.Check, null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }) else null
                        )
                    }
                }
            }
        }
    }
}

// ── SettingsActionItem ────────────────────────────────────────
@Composable
private fun SettingsActionItem(
    title:   String,
    subtitle: String? = null,
    icon:    ImageVector,
    tint:    androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color   = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = tint
                )
                if (subtitle != null) {
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight, null,
                tint     = tint.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── SettingsInfoItem ──────────────────────────────────────────
@Composable
private fun SettingsInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── SettingsDivider ───────────────────────────────────────────
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color     = Color.Transparent
    )
}

// ── Helper formatters ─────────────────────────────────────────
private fun formatRate(rate: Float) = when {
    rate <= 0.5f -> "0.5× slow"
    rate <= 0.75f -> "0.75×"
    rate <= 1.0f -> "1× normal"
    rate <= 1.25f -> "1.25×"
    rate <= 1.5f -> "1.5× fast"
    rate <= 1.75f -> "1.75×"
    else -> "2× fastest"
}

private fun formatPitch(pitch: Float) = when {
    pitch <= 0.5f -> "Deep"
    pitch <= 0.75f -> "Low"
    pitch <= 1.0f -> "Normal"
    pitch <= 1.25f -> "High"
    else -> "Very high"
}