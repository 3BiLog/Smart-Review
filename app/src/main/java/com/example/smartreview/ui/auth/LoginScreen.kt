package com.example.smartreview.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartreview.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateSignUp: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val fieldsEnabled = !state.isLoading
    val emailValid = state.email.contains("@") && state.email.contains(".")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        AuthBackgroundOrbs()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            AuthBrandSection(
                subtitle = "Đăng nhập để đồng bộ khóa học, cộng đồng và hồ sơ của bạn",
            )

            Spacer(Modifier.height(28.dp))

            AuthScreenHeader(
                title = "Đăng nhập",
                subtitle = "Sử dụng email và mật khẩu đã đăng ký trên SmartReview",
            )

            Spacer(Modifier.height(20.dp))

            AuthFormCard {
                AuthTextField(
                    value = state.email,
                    onValueChange = vm::onEmailChange,
                    label = "Email",
                    leadingIcon = Icons.Default.Mail,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = fieldsEnabled,
                    isError = state.email.isNotBlank() && !emailValid,
                    supportingText = when {
                        state.email.isBlank() -> "Nhập email đã đăng ký"
                        !emailValid -> "Email không hợp lệ"
                        else -> null
                    },
                )

                AuthPasswordField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    label = "Mật khẩu",
                    isVisible = state.isPasswordVisible,
                    onToggleVisibility = vm::togglePasswordVisibility,
                    imeAction = ImeAction.Done,
                    onImeAction = { if (state.isLoginFormValid) vm.login(onLoginSuccess) },
                    enabled = fieldsEnabled,
                    supportingText = if (state.password.isNotEmpty() && state.password.length < AuthUiState.MIN_PASSWORD_LENGTH) {
                        "Tối thiểu ${AuthUiState.MIN_PASSWORD_LENGTH} ký tự"
                    } else null,
                    isError = state.password.isNotEmpty() && state.password.length < AuthUiState.MIN_PASSWORD_LENGTH,
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Quên mật khẩu?",
                        color = Primary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                state.error?.let { err -> AuthErrorBanner(err) }

                GradientAuthButton(
                    text = "Đăng nhập",
                    onClick = { vm.login(onLoginSuccess) },
                    enabled = state.isLoginFormValid && fieldsEnabled,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))

            AuthDivider(modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            SocialLoginButton(
                label = "Google",
                onClick = { vm.showSocialLoginUnavailable() },
                modifier = Modifier.fillMaxWidth(),
                enabled = fieldsEnabled,
            )

            Text(
                text = "Đăng nhập mạng xã hội sẽ được bổ sung trong phiên bản sau.",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(Modifier.height(24.dp))

            AuthFooterText(
                prefix = "Chưa có tài khoản?",
                linkText = "Đăng ký ngay",
                onClick = onNavigateSignUp,
            )

            Spacer(Modifier.height(16.dp))
        }

        AuthLoadingOverlay(isLoading = state.isLoading, message = "Đang đăng nhập...")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateSignUp = {})
    }
}
