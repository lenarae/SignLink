package com.signlink.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*

// ── Data models for learning content ─────────────────────────
data class LessonCategory(
    val title:       String,
    val icon:        String,
    val description: String,
    val lessonCount: Int,
    val isAvailable: Boolean = false
)

data class SignPreview(
    val sign:        String,
    val emoji:       String,
    val difficulty:  String
)

// ── Content data ──────────────────────────────────────────────
private val LESSON_CATEGORIES = listOf(
    LessonCategory("Alphabet",       "🔤", "Learn all 26 handshape letters (A–Z)",    26, false),
    LessonCategory("Numbers",        "🔢", "Count from 0 to 100 in ASL",              12, false),
    LessonCategory("Greetings",      "👋", "Everyday hello, goodbye, thank you",       8,  false),
    LessonCategory("Common Phrases", "💬", "Essential daily communication signs",      20, false),
    LessonCategory("Emotions",       "😊", "Express feelings and emotions",            15, false),
    LessonCategory("Family",         "👨‍👩‍👧", "Family member signs",                     10, false)
)

private val SIGN_PREVIEWS = listOf(
    SignPreview("Hello",      "👋", "Beginner"),
    SignPreview("Yes",        "✊", "Beginner"),
    SignPreview("No",         "✌️", "Beginner"),
    SignPreview("Thank you",  "🙏", "Beginner"),
    SignPreview("Please",     "🤲", "Beginner"),
    SignPreview("Help",       "🆘", "Beginner"),
    SignPreview("Love",       "🤟", "Beginner"),
    SignPreview("Sorry",      "✊", "Beginner")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(navController: NavHostController) {

    // Pulsing animation for the "coming soon" badge
    val infiniteTransition = rememberInfiniteTransition(label = "badge")
    val badgePulse by infiniteTransition.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Learning Mode",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Hero banner ───────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SignLinkTeal700, SignLinkTeal500, SignLinkCyanDark)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📚", fontSize = 48.sp)
                        Text(
                            text      = "Learn Sign Language",
                            style     = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color     = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text      = "Interactive lessons powered by your SignLink wristband — coming soon.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = SignLinkTeal100,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        // Coming soon badge (pulsing)
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color.White.copy(alpha = 0.2f * badgePulse)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SignLinkCyan)
                                )
                                Text(
                                    "Feature in development",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── Quick signs preview ───────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text  = "Signs you can already translate 🤟",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text  = "Your wristband already recognises these — tap Translation to try them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding        = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(SIGN_PREVIEWS) { sign ->
                            SignPreviewCard(sign = sign)
                        }
                    }
                }
            }

            // ── Planned lesson categories ─────────────────────
            item {
                Text(
                    text  = "Planned lessons",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            items(LESSON_CATEGORIES) { category ->
                LessonCategoryCard(category = category)
            }

            // ── Roadmap card ──────────────────────────────────
            item {
                RoadmapCard()
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── SignPreviewCard ───────────────────────────────────────────
@Composable
private fun SignPreviewCard(sign: SignPreview) {
    Card(
        modifier  = Modifier.width(90.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(sign.emoji, fontSize = 24.sp)
            Text(
                text      = sign.sign,
                style     = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color     = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text  = sign.difficulty,
                style = MaterialTheme.typography.labelSmall,
                color = SignLinkTheme.colors.success
            )
        }
    }
}

// ── LessonCategoryCard ────────────────────────────────────────
@Composable
private fun LessonCategoryCard(category: LessonCategory) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (category.isAvailable)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(category.icon, fontSize = 22.sp)
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = category.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (category.isAvailable)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (!category.isAvailable) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text     = "Soon",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text  = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "${category.lessonCount} lessons",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (category.isAvailable) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Lock / chevron
            Icon(
                imageVector = if (category.isAvailable) Icons.Filled.ChevronRight
                else                       Icons.Filled.Lock,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── RoadmapCard ───────────────────────────────────────────────
@Composable
private fun RoadmapCard() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Map, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                Text(
                    text  = "Development Roadmap",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            val roadmapItems = listOf(
                "✅" to "Real-time gesture translation (live)",
                "✅" to "Speech-to-text input (live)",
                "✅" to "Local chat history with search",
                "🔄" to "AI gesture classifier (replacing mock)",
                "⏳" to "Interactive sign lessons",
                "⏳" to "Progress tracking & daily streaks",
                "⏳" to "Cloud sync & multi-device support",
                "⏳" to "Community sign library"
            )

            roadmapItems.forEach { (status, item) ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(status, fontSize = 14.sp)
                    Text(
                        text  = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                            alpha = if (status == "✅") 1f else 0.7f
                        )
                    )
                }
            }
        }
    }
}