package com.example.smartreview.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartreview.ui.theme.*

@Composable
fun SignUpScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val fieldsEnabled = !state.isLoading
    val showPasswordMismatch = state.confirmPassword.isNotEmpty() && !state.passwordsMatch
    val fullNameOk = state.fullName.trim().length >= AuthUiState.MIN_NAME_LENGTH
    val emailOk = state.email.contains("@") && state.email.contains(".")
    val passwordOk = state.password.length >= AuthUiState.MIN_PASSWORD_LENGTH
    val passwordsMatch = state.passwordsMatch

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        AuthBackgroundOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            AuthTopBar(title = "Đăng ký", onBack = onNavigateLogin)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                AuthScreenHeader(
                    title = "Tạo tài khoản",
                    subtitle = "Đăng ký bằng email — đồng bộ hồ sơ Firestore ngay sau khi tạo",
                )

                Spacer(Modifier.height(20.dp))

                AuthFormCard {
                    AuthTextField(
                        value = state.fullName,
                        onValueChange = vm::onFullNameChange,
                        label = "Họ và tên",
                        leadingIcon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        enabled = fieldsEnabled,
                        isError = state.fullName.isNotBlank() && !fullNameOk,
                        supportingText = when {
                            state.fullName.isBlank() -> "Hiển thị trên hồ sơ và bảng xếp hạng"
                            !fullNameOk -> "Tối thiểu ${AuthUiState.MIN_NAME_LENGTH} ký tự"
                            else -> null
                        },
                    )

                    AuthTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = "Email",
                        leadingIcon = Icons.Default.Mail,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        enabled = fieldsEnabled,
                        isError = state.email.isNotBlank() && !emailOk,
                        supportingText = if (state.email.isNotBlank() && !emailOk) "Email không hợp lệ" else null,
                    )

                    AuthPasswordField(
                        value = state.password,
                        onValueChange = vm::onPasswordChange,
                        label = "Mật khẩu",
                        isVisible = state.isPasswordVisible,
                        onToggleVisibility = vm::togglePasswordVisibility,
                        imeAction = ImeAction.Next,
                        enabled = fieldsEnabled,
                    )

                    if (state.password.isNotEmpty()) {
                        AuthPasswordStrengthBar(password = state.password)
                    }

                    AuthPasswordField(
                        value = state.confirmPassword,
                        onValueChange = vm::onConfirmPasswordChange,
                        label = "Xác nhận mật khẩu",
                        isVisible = state.isConfirmPasswordVisible,
                        onToggleVisibility = vm::toggleConfirmPasswordVisibility,
                        imeAction = ImeAction.Done,
                        onImeAction = { if (state.isSignUpFormValid) vm.register(onRegisterSuccess) },
                        enabled = fieldsEnabled,
                        isError = showPasswordMismatch,
                        supportingText = when {
                            showPasswordMismatch -> "Mật khẩu xác nhận không khớp"
                            else -> "Nhập lại mật khẩu để xác nhận"
                        },
                    )

                    AuthSignUpChecklist(
                        fullNameOk = fullNameOk,
                        emailOk = emailOk,
                        passwordOk = passwordOk,
                        passwordsMatch = passwordsMatch,
                        termsOk = state.termsAccepted,
                    )

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Checkbox(
                            checked = state.termsAccepted,
                            onCheckedChange = { vm.onTermsToggle() },
                            enabled = fieldsEnabled,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Primary,
                                uncheckedColor = OnSurfaceVariant,
                            ),
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Tôi đồng ý với ")
                                withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.SemiBold)) {
                                    append("Điều khoản & Chính sách")
                                }
                                append(" của SmartReview.")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    state.error?.let { err -> AuthErrorBanner(err) }

                    GradientAuthButton(
                        text = "Tạo tài khoản",
                        onClick = { vm.register(onRegisterSuccess) },
                        enabled = state.isSignUpFormValid && fieldsEnabled,
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(20.dp))

                AuthDivider(label = "Hoặc đăng ký với", modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SocialLoginButton(
                        label = "Google",
                        onClick = { vm.showSocialLoginUnavailable() },
                        modifier = Modifier.weight(1f),
                        enabled = fieldsEnabled,
                    )
                    SocialLoginButton(
                        label = "Apple",
                        onClick = { vm.showSocialLoginUnavailable() },
                        modifier = Modifier.weight(1f),
                        enabled = fieldsEnabled,
                    )
                }

                Spacer(Modifier.height(24.dp))

                AuthFooterText(
                    prefix = "Đã có tài khoản?",
                    linkText = "Đăng nhập",
                    onClick = onNavigateLogin,
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        AuthLoadingOverlay(isLoading = state.isLoading, message = "Đang tạo tài khoản...")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun SignUpPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        SignUpScreen(onRegisterSuccess = {}, onNavigateLogin = {})
    }
}
