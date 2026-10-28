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
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon          = {
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    tint               = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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
                focusedTextColor      = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor    = MaterialTheme.colorScheme.onSurface,
                errorTextColor        = MaterialTheme.colorScheme.onSurface,

                // Container (background) — semi-transparent
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                errorContainerColor     = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),

                // Border colors (Flat design: no borders)
                focusedBorderColor   = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor     = Color.Transparent,
                disabledBorderColor  = Color.Transparent,

                // Label colors
                focusedLabelColor   = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLabelColor     = MaterialTheme.colorScheme.error,

                // Cursor
                cursorColor      = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error
            )
        )

        // Error message below the field
        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "⚠ $errorMessage",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}