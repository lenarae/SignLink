package com.signlink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
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
import com.signlink.app.utils.TtsState
import com.signlink.app.viewmodel.TextToSpeechViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToSpeechScreen(navController: NavHostController) {
    val viewModel: TextToSpeechViewModel = hiltViewModel()
    val textInput by viewModel.textInput.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Text to Speech",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
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
                    .offset(x = 150.dp, y = 100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            "Type something below...",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = textInput,
                            onValueChange = { viewModel.onTextChanged(it) },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = {
                                Text(
                                    "e.g. Hello, how can I help you?",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clear Button
                    OutlinedButton(
                        onClick = { viewModel.clearText() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.ClearAll, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear")
                    }

                    // Speak Button
                    Button(
                        onClick = {
                            if (ttsState == TtsState.SPEAKING) viewModel.stopSpeaking()
                            else viewModel.speak()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = textInput.isNotBlank() || ttsState == TtsState.SPEAKING
                    ) {
                        Icon(
                            imageVector = if (ttsState == TtsState.SPEAKING) Icons.Filled.Stop
                            else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (ttsState == TtsState.SPEAKING) "Stop" else "Speak Out")
                    }
                }

                if (ttsState == TtsState.SPEAKING) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Speaking...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(32.dp)) // Placeholder height to prevent jump
                }
            }
        }
    }
}
