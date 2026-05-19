package com.example.smartreview.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartreview.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess:   () -> Unit,
    onNavigateSignUp: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        AuthBackgroundOrbs()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {

            // ── Brand ─────────────────────────────────────────────────────
            AuthBrandSection()

            Spacer(Modifier.height(32.dp))

            // ── Glass form card ───────────────────────────────────────────
            Surface(
                color    = GlassBg,
                shape    = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier            = Modifier.padding(24.dp),
                ) {

                    // Email
                    AuthTextField(
                        value         = state.email,
                        onValueChange = vm::onEmailChange,
                        label         = "Email",
                        leadingIcon   = Icons.Default.Mail,
                        keyboardType  = KeyboardType.Email,
                        imeAction     = ImeAction.Next,
                    )

                    // Password
                    AuthPasswordField(
                        value               = state.password,
                        onValueChange       = vm::onPasswordChange,
                        label               = "Mật khẩu",
                        isVisible           = state.isPasswordVisible,
                        onToggleVisibility  = vm::togglePasswordVisibility,
                        imeAction           = ImeAction.Done,
                        onImeAction         = { vm.login(onLoginSuccess) },
                    )

                    // Forgot password
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Quên mật khẩu?",
                            color  = Primary,
                            style  = MaterialTheme.typography.labelSmall,
                        )
                    }

                    // Error message
                    state.error?.let { err ->
                        Text(
                            err,
                            color  = ErrorColor,
                            style  = MaterialTheme.typography.labelSmall,
                        )
                    }

                    // Login button
                    GradientAuthButton(
                        text      = "Đăng Nhập →",
                        onClick   = { vm.login(onLoginSuccess) },
                        enabled   = state.isLoginFormValid,
                        isLoading = state.isLoading,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Divider + Social ──────────────────────────────────────────
            AuthDivider(modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            SocialLoginButton(
                label    = "Google",
                onClick  = { onLoginSuccess() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // ── Footer ────────────────────────────────────────────────────
            AuthFooterText(
                prefix   = "Chưa có tài khoản?",
                linkText = "Đăng ký ngay",
                onClick  = onNavigateSignUp,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateSignUp = {})
    }
}