// ============================================================
// File: ui/screens/AuthTextField.kt
// Purpose: A reusable text field component for auth screens.
//
// Styled to match the dark teal background of Welcome/Login/Register.
// Handles:
//   - Leading icon
//   - Optional trailing icon (e.g. password visibility toggle)
//   - Error state (red border + error message below)
//   - Custom visual transformation (for password masking)
//
// WHY a separate composable?
//   LoginScreen and RegisterScreen both need styled text fields.
//   Instead of duplicating 30+ lines of OutlinedTextField styling,
//   we extract it here once. This is the DRY (Don't Repeat Yourself) principle.
// ============================================================

package com.signlink.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.signlink.app.ui.theme.*

/**
 * A styled text field for the SignLink auth screens.
 *
 * @param value              Current field value
 * @param onValueChange      Called when user types
 * @param label              Placeholder / floating label text
 * @param leadingIcon        Icon shown on the left
 * @param trailingIcon       Optional composable for right side (e.g. eye icon)
 * @param visualTransformation PasswordVisualTransformation or None
 * @param keyboardOptions    Keyboard type, IME action
 * @param keyboardActions    What happens on IME action (Next / Done)
 * @param errorMessage       If non-null, shown in red below the field
 * @param modifier           External layout modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value:                String,
    onValueChange:        (String) -> Unit,
    label:                String,
    leadingIcon:          ImageVector,
    trailingIcon:         (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation      = VisualTransformation.None,
    keyboardOptions:      KeyboardOptions           = KeyboardOptions.Default,
    keyboardActions:      KeyboardActions           = KeyboardActions.Default,
    errorMessage:         String?                   = null,
    modifier:             Modifier                  = Modifier.fillMaxWidth()
) {
    // isError = true triggers red border and error icon automatically
    val isError = errorMessage != null

    Column(modifier = modifier) {
        OutlinedTextField(
            value                = value,
            onValueChange        = onValueChange,
            label                = {
                Text(
                    text  = label,
                    color = if (isError) SignLinkDisconnected else SignLinkTeal400
                )
            },
            leadingIcon          = {
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    tint               = if (isError) SignLinkDisconnected else SignLinkTeal400,
                    modifier           = Modifier.size(20.dp)
                )
            },
            trailingIcon         = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions      = keyboardOptions,
            keyboardActions      = keyboardActions,
            isError              = isError,
            singleLine           = true,
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(12.dp),
            colors               = OutlinedTextFieldDefaults.colors(
                // Text color
                focusedTextColor      = Color.White,
                unfocusedTextColor    = Color.White,
                errorTextColor        = Color.White,

                // Container (background) — semi-transparent white
                focusedContainerColor   = SignLinkTeal800.copy(alpha = 0.5f),
                unfocusedContainerColor = SignLinkTeal800.copy(alpha = 0.3f),
                errorContainerColor     = SignLinkTeal800.copy(alpha = 0.3f),

                // Border colors
                focusedBorderColor   = SignLinkCyan,
                unfocusedBorderColor = SignLinkTeal600,
                errorBorderColor     = SignLinkDisconnected,

                // Label colors
                focusedLabelColor   = SignLinkCyan,
                unfocusedLabelColor = SignLinkTeal400,
                errorLabelColor     = SignLinkDisconnected,

                // Cursor
                cursorColor      = SignLinkCyan,
                errorCursorColor = SignLinkDisconnected
            )
        )

        // Error message below the field
        if (isError && errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "⚠ $errorMessage",
                style = MaterialTheme.typography.labelSmall,
                color = SignLinkDisconnected
            )
        }
    }
}