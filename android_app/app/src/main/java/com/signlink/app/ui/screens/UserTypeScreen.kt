package com.signlink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTypeScreen(navController: NavHostController) {
    var selectedType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(
                        onClick = { navController.navigateUp() },
                        contentPadding = PaddingValues(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF5F6368),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Back",
                            color = Color(0xFF5F6368),
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .padding(top = 15.dp, bottom = 15.dp), // Added top/bottom padding for icon spacing
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // ── HEADER ────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tell Us About Yourself",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "This helps us personalize your experience and enable the right features.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF5F6368),
                    lineHeight = 22.sp
                )
            }

            // ── INFO BOX ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F0FE))
                    .border(1.dp, Color(0xFFD2E3FC), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF1967D2),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "You can change this later in Settings. Your choice affects which features are shown.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1967D2),
                        lineHeight = 20.sp
                    )
                }
            }

            // ── USER TYPE CARDS ───────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserTypeCard(
                    title = "Deaf User",
                    description = "I use sign language and want to translate it to text and speech for others to understand.",
                    icon = Icons.Default.HearingDisabled,
                    isSelected = selectedType == "Deaf",
                    onClick = { selectedType = "Deaf" }
                )

                UserTypeCard(
                    title = "Mute User",
                    description = "I cannot speak and want to use sign language or gestures to communicate with others.",
                    icon = Icons.Default.Mic,
                    isSelected = selectedType == "Mute",
                    onClick = { selectedType = "Mute" }
                )

                UserTypeCard(
                    title = "Hearing User",
                    description = "I can hear and speak, and want to communicate with deaf or mute individuals.",
                    icon = Icons.Default.Hearing,
                    isSelected = selectedType == "Hearing",
                    onClick = { selectedType = "Hearing" }
                )

                UserTypeCard(
                    title = "Learner",
                    description = "I want to learn sign language and practice with feedback and tutorials.",
                    icon = Icons.Default.School,
                    isSelected = selectedType == "Learner",
                    onClick = { selectedType = "Learner" }
                )
            }

            // ── CONTINUE BUTTON ───────────────────────────────
            Button(
                onClick = { navController.navigate(Screen.PairDevice.route) },
                enabled = selectedType != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType != null) Color(0xFF1A73E8) else Color(0xFFF1F3F4),
                    contentColor = if (selectedType != null) Color.White else Color(0xFF9AA0A6)
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun UserTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF1A73E8) else Color(0xFFDADCE0)
    val iconBgColor = if (isSelected) Color(0xFF1A73E8) else Color(0xFFF1F3F4)
    val iconTintColor = if (isSelected) Color.White else Color(0xFF5F6368)
    val titleColor = if (isSelected) Color(0xFF1A73E8) else Color.Black
    val descColor = if (isSelected) Color(0xFF1A73E8).copy(alpha = 0.8f) else Color(0xFF5F6368)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp) // Fixed height to prevent shifting
            .border(
                width = 2.dp, // Constant width to prevent shifting
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon square box
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = titleColor,
                        maxLines = 1
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = descColor,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                // Placeholder space for checkmark so text doesn't shift
                Spacer(Modifier.width(32.dp))
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1A73E8),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
        }
    }
}
