// ============================================================
// File: ui/screens/WelcomeScreen.kt
// Purpose: The first screen new users see.
//
// Design goals:
//   - Animated logo entrance (scale + fade)
//   - Staggered text reveal (headline, then subtitle)
//   - Two CTAs: Get Started (→ Register) and Sign In (→ Login)
//   - Floating sign-language "hand" shapes as background art
//   - Works beautifully in both light and dark mode
//
// Nielsen heuristics applied:
//   - #1: Status shown (app name + tagline = what this app is)
//   - #4: Consistency (uses SignLink brand colors)
//   - #6: Recognition over recall (clear CTA labels)
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignLanguage
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
import androidx.navigation.NavHostController
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import kotlinx.coroutines.delay

// ── WelcomeScreen ──────────────────────────────────────────────
/**
 * The animated welcome screen shown to new / logged-out users.
 *
 * Animation sequence:
 *   0ms   → background gradient fades in
 *   300ms → logo circle scales up from 0 → 1
 *   600ms → app name slides up + fades in
 *   900ms → tagline slides up + fades in
 *   1200ms→ feature pills appear one by one
 *   1600ms→ CTA buttons slide up from bottom
 */
@Composable
fun WelcomeScreen(navController: NavHostController) {

    // ── Animation state flags ─────────────────────────────────
    // Each flag triggers its element's entrance animation
    var showLogo     by remember { mutableStateOf(false) }
    var showHeadline by remember { mutableStateOf(false) }
    var showTagline  by remember { mutableStateOf(false) }
    var showPills    by remember { mutableStateOf(false) }
    var showButtons  by remember { mutableStateOf(false) }

    // ── Staggered animation launch ────────────────────────────
    // LaunchedEffect runs once when the screen first appears
    LaunchedEffect(Unit) {
        showLogo     = true
        delay(300)
        showHeadline = true
        delay(300)
        showTagline  = true
        delay(300)
        showPills    = true
        delay(400)
        showButtons  = true
    }

    // ── Floating orb animations (background decoration) ───────
    // These create gentle floating circles in the background
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val orb2Y by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue  = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    // ── Background gradient ───────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SignLinkTeal900,
                        SignLinkTeal800,
                        Color(0xFF001D26)
                    )
                )
            )
    ) {

        // ── Decorative background orbs ─────────────────────────
        // These soft circles add depth to the dark background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (orb1Y - 60).dp)
                .clip(CircleShape)
                .background(SignLinkTeal600.copy(alpha = 0.15f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (orb2Y + 100).dp)
                .clip(CircleShape)
                .background(SignLinkCyan.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = (40).dp)
                .clip(CircleShape)
                .background(SignLinkTeal500.copy(alpha = 0.1f))
        )

        // ── Main content column ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── TOP: Logo + Text section ───────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── Logo ────────────────────────────────────────
                // Animated scale-in from center
                AnimatedVisibility(
                    visible = showLogo,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    // Outer ring (glow effect)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SignLinkTeal500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner filled circle
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            SignLinkCyan,
                                            SignLinkTeal500
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SignLanguage,
                                contentDescription = "SignLink logo",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                // ── App Name ─────────────────────────────────────
                AnimatedVisibility(
                    visible = showHeadline,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(
                        animationSpec = tween(400)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SignLink",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Cyan accent underline bar
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SignLinkCyan)
                        )
                    }
                }

                // ── Tagline ───────────────────────────────────────
                AnimatedVisibility(
                    visible = showTagline,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(
                        animationSpec = tween(400)
                    )
                ) {
                    Text(
                        text = "Bridging the gap between\nsign language and speech",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SignLinkTeal200,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Feature Pills ─────────────────────────────────
                // Compact chips showing key features at a glance
                AnimatedVisibility(
                    visible = showPills,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FeaturePill(emoji = "🤟", label = "Sign → Text")
                            FeaturePill(emoji = "🔊", label = "Text-to-Speech")
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FeaturePill(emoji = "📡", label = "Wristband BLE")
                            FeaturePill(emoji = "💬", label = "Chat History")
                        }
                    }
                }
            }

            // ── BOTTOM: CTA Buttons ────────────────────────────
            AnimatedVisibility(
                visible = showButtons,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec  = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMediumLow
                    )
                ) + fadeIn()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Primary CTA: Get Started → Register
                    Button(
                        onClick = { navController.navigate(Screen.Register.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignLinkCyan,
                            contentColor   = SignLinkTeal900
                        )
                    ) {
                        Text(
                            text       = "Get Started",
                            style      = MaterialTheme.typography.labelLarge.copy(
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Secondary CTA: Sign In → Login
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Login.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = SignLinkTeal400
                        )
                    ) {
                        Text(
                            text  = "I already have an account",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp
                            )
                        )
                    }

                    // Version / legal note
                    Text(
                        text      = "SignLink v1.0 · Your privacy is protected",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = SignLinkTeal400,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ── FeaturePill ────────────────────────────────────────────────
/**
 * A small pill-shaped chip showing an emoji + label.
 * Used on the welcome screen to communicate features quickly.
 */
@Composable
private fun FeaturePill(emoji: String, label: String) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = SignLinkTeal700.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = SignLinkTeal100
            )
        }
    }
}