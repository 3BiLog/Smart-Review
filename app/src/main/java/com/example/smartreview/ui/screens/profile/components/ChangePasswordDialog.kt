package com.example.smartreview.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.theme.Primary
import com.example.smartreview.ui.theme.SurfaceContainer

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (currentPassword: String, newPassword: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    val isFormValid = currentPassword.isNotBlank() &&
            newPassword.length >= 6 &&
            newPassword == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Đổi mật khẩu",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Current password
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        localError = null
                    },
                    label = { Text("Mật khẩu hiện tại") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = if (showCurrentPassword)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { showCurrentPassword = !showCurrentPassword },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(if (showCurrentPassword) "ẨN" else "HIỆN", fontSize = 10.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),  // ✅ Sửa
                    shape = RoundedCornerShape(12.dp),
                    isError = localError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // New password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        localError = null
                    },
                    label = { Text("Mật khẩu mới") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = if (showNewPassword)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { showNewPassword = !showNewPassword },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(if (showNewPassword) "ẨN" else "HIỆN", fontSize = 10.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),  // ✅ Sửa
                    shape = RoundedCornerShape(12.dp),
                    isError = newPassword.isNotBlank() && newPassword.length < 6,
                    supportingText = {
                        if (newPassword.isNotBlank() && newPassword.length < 6) {
                            Text("Mật khẩu tối thiểu 6 ký tự")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        localError = null
                    },
                    label = { Text("Xác nhận mật khẩu mới") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = if (showConfirmPassword)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { showConfirmPassword = !showConfirmPassword },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(if (showConfirmPassword) "ẨN" else "HIỆN", fontSize = 10.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),  // ✅ Sửa
                    shape = RoundedCornerShape(12.dp),
                    isError = confirmPassword.isNotBlank() && newPassword != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                            Text("Mật khẩu xác nhận không khớp")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                (localError ?: errorMessage)?.let { error ->
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        localError = "Mật khẩu mới phải có ít nhất 6 ký tự"
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        localError = "Mật khẩu xác nhận không khớp"
                        return@Button
                    }
                    onConfirm(currentPassword, newPassword)
                },
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Đổi mật khẩu")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = SurfaceContainer
    )
}