package com.signlink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {

    // ── Form state ────────────────────────────────────────────
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading      by remember { mutableStateOf(false) }
    var emailError     by remember { mutableStateOf<String?>(null) }
    var passwordError  by remember { mutableStateOf<String?>(null) }

    val focusManager  = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // ── Form validation ───────────────────────────────────────
    fun validate(): Boolean {
        var valid = true

        // Email must contain "@" and "."
        emailError = if (email.isBlank()) {
            valid = false; "Email is required"
        } else if (!email.contains("@") || !email.contains(".")) {
            valid = false; "Enter a valid email address"
        } else null

        // Password must be at least 6 chars
        passwordError = if (password.isBlank()) {
            valid = false; "Password is required"
        } else if (password.length < 6) {
            valid = false; "Password must be at least 6 characters"
        } else null

        return valid
    }

    // ── Submit handler ────────────────────────────────────────
    fun onSignIn() {
        focusManager.clearFocus()
        if (!validate()) return

        coroutineScope.launch {
            isLoading = true
            delay(1500)   // Replace with real auth call in future
            isLoading = false
            // Navigate to Home, clearing the back stack so Back doesn't return to Login
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SignLinkTeal900,
                        Color(0xFF001D26)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // ── Header ────────────────────────────────────
                Text(
                    text      = "Welcome back",
                    style     = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color     = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Sign in to continue to SignLink",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SignLinkTeal300
                )

                Spacer(Modifier.height(40.dp))

                // ── Email Field ───────────────────────────────
                AuthTextField(
                    value         = email,
                    onValueChange = { email = it; emailError = null },
                    label         = "Email address",
                    leadingIcon   = Icons.Filled.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    errorMessage  = emailError
                )

                Spacer(Modifier.height(16.dp))

                // ── Password Field ────────────────────────────
                AuthTextField(
                    value         = password,
                    onValueChange = { password = it; passwordError = null },
                    label         = "Password",
                    leadingIcon   = Icons.Filled.Lock,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon  = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "Hide password"
                                else
                                    "Show password",
                                tint = SignLinkTeal400
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSignIn() }
                    ),
                    errorMessage  = passwordError
                )

                // ── Forgot Password ───────────────────────────
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = { /* Phase: add password reset */ }) {
                        Text(
                            text  = "Forgot password?",
                            style = MaterialTheme.typography.bodySmall,
                            color = SignLinkCyan
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Sign In Button ────────────────────────────
                Button(
                    onClick  = { onSignIn() },
                    enabled  = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = SignLinkCyan,
                        contentColor           = SignLinkTeal900,
                        disabledContainerColor = SignLinkTeal600.copy(alpha = 0.5f),
                        disabledContentColor   = SignLinkTeal300
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(22.dp),
                            color     = SignLinkTeal900,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text       = "Sign In",
                            style      = MaterialTheme.typography.labelLarge.copy(
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}