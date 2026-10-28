package com.signlink.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.signlink.app.navigation.Screen
import com.signlink.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {

    // ── Form state ────────────────────────────────────────────
    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }
    var agreedToTerms    by remember { mutableStateOf(false) }
    var isLoading        by remember { mutableStateOf(false) }

    // Error states per field
    var nameError    by remember { mutableStateOf<String?>(null) }
    var emailError   by remember { mutableStateOf<String?>(null) }
    var passError    by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var termsError   by remember { mutableStateOf(false) }

    val focusManager   = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // ── Password strength calculator ──────────────────────────
    // Returns 0–4: 0=empty, 1=weak, 2=fair, 3=good, 4=strong
    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> 0
            password.length < 6 -> 1
            password.length < 8 -> 2
            password.length >= 8 && (password.any { it.isDigit() } ||
                    password.any { !it.isLetterOrDigit() }) -> 4
            else -> 3
        }
    }

    // ── Validate all fields ───────────────────────────────────
    fun validate(): Boolean {
        var valid = true

        nameError = if (fullName.isBlank()) {
            valid = false; "Name is required"
        } else null

        emailError = if (email.isBlank()) {
            valid = false; "Email is required"
        } else if (!email.contains("@")) {
            valid = false; "Enter a valid email"
        } else null

        passError = if (password.isBlank()) {
            valid = false; "Password is required"
        } else if (password.length < 6) {
            valid = false; "Must be at least 6 characters"
        } else null

        confirmError = if (confirmPassword != password) {
            valid = false; "Passwords do not match"
        } else null

        termsError = !agreedToTerms
        if (!agreedToTerms) valid = false

        return valid
    }

    // ── Submit ────────────────────────────────────────────────
    fun onCreateAccount() {
        focusManager.clearFocus()
        if (!validate()) return
        coroutineScope.launch {
            isLoading = true
            delay(1500)   // Simulate API call
            isLoading = false
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
                    colors = if (isSystemInDarkTheme()) {
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.background
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surface
                        )
                    }
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
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
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
                Spacer(Modifier.height(16.dp))

                // ── Header ────────────────────────────────────
                Text(
                    text  = "Create account",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = "Join SignLink to start translating",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                // ── Full Name ─────────────────────────────────
                AuthTextField(
                    value         = fullName,
                    onValueChange = { fullName = it; nameError = null },
                    label         = "Full name",
                    leadingIcon   = Icons.Filled.Person,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    errorMessage = nameError
                )

                Spacer(Modifier.height(14.dp))

                // ── Email ─────────────────────────────────────
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
                    errorMessage = emailError
                )

                Spacer(Modifier.height(14.dp))

                // ── Password ──────────────────────────────────
                AuthTextField(
                    value         = password,
                    onValueChange = { password = it; passError = null },
                    label         = "Password",
                    leadingIcon   = Icons.Filled.Lock,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    errorMessage = passError
                )

                // ── Password Strength Indicator ───────────────
                if (password.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    PasswordStrengthBar(strength = passwordStrength)
                }

                Spacer(Modifier.height(14.dp))

                // ── Confirm Password ──────────────────────────
                AuthTextField(
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmError = null },
                    label         = "Confirm password",
                    leadingIcon   = Icons.Filled.LockOpen,
                    visualTransformation = if (confirmVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                            imageVector = if (confirmVisible)
                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle confirm password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onCreateAccount() }
                    ),
                    errorMessage = confirmError
                )

                Spacer(Modifier.height(20.dp))

                // ── Terms checkbox ────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked  = agreedToTerms,
                        onCheckedChange = {
                            agreedToTerms = it
                            termsError = false
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor   = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedColor = if (termsError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "I agree to the Terms of Service and Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (termsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Create Account Button ─────────────────────
                Button(
                    onClick  = { onCreateAccount() },
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
                        containerColor         = MaterialTheme.colorScheme.primary,
                        contentColor           = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text       = "Create Account",
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

// ── Password Strength Bar ──────────────────────────────────────
@Composable
private fun PasswordStrengthBar(strength: Int) {
    val strengthLabel = when (strength) {
        1    -> "Weak"
        2    -> "Fair"
        3    -> "Good"
        4    -> "Strong"
        else -> ""
    }
    val strengthColor = when (strength) {
        1    -> MaterialTheme.colorScheme.error
        2    -> Warning
        3    -> Color(0xFFFFD700)
        4    -> Success
        else -> MaterialTheme.colorScheme.outline
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {


            repeat(4) { index ->
                val filled       = index < strength
                val segmentColor by animateColorAsState(
                    targetValue  = if (filled) strengthColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    animationSpec = tween(300),
                    label        = "segment$index"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segmentColor)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = strengthLabel,
            style = MaterialTheme.typography.labelSmall,
            color = strengthColor
        )
    }
}