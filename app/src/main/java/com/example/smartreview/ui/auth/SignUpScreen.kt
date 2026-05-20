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
    onNavigateVerify: () -> Unit,
    onNavigateLogin:  () -> Unit,
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
            AuthBrandSection(
                icon     = Icons.Default.School,
                subtitle = "Enter your details below & free sign up",
            )

            Spacer(Modifier.height(28.dp))

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
                        label         = "Your Email",
                        leadingIcon   = Icons.Default.Mail,
                        keyboardType  = KeyboardType.Email,
                    )

                    // Password
                    AuthPasswordField(
                        value              = state.password,
                        onValueChange      = vm::onPasswordChange,
                        label              = "Password",
                        isVisible          = state.isPasswordVisible,
                        onToggleVisibility = vm::togglePasswordVisibility,
                        imeAction          = ImeAction.Done,
                    )

                    // Terms checkbox
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Checkbox(
                            checked         = state.termsAccepted,
                            onCheckedChange = { vm.onTermsToggle() },
                            colors          = CheckboxDefaults.colors(
                                checkedColor   = Primary,
                                uncheckedColor = OnSurfaceVariant,
                            ),
                        )
                        Text(
                            text  = buildAnnotatedString {
                                append("By creating an account you have to agree with our ")
                                withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.SemiBold)) {
                                    append("Terms & Conditions.")
                                }
                            },
                            style    = MaterialTheme.typography.bodySmall,
                            color    = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    // Error
                    state.error?.let { err ->
                        Text(err, color = ErrorColor, style = MaterialTheme.typography.labelSmall)
                    }

                    // Sign up button
                    GradientAuthButton(
                        text      = "Create account →",
                        onClick   = { vm.register(onNavigateVerify) },
                        enabled   = state.isSignUpFormValid,
                        isLoading = state.isLoading,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Divider + Social ──────────────────────────────────────────
            AuthDivider(label = "Or sign up with", modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SocialLoginButton("Google", onClick = { vm.showSocialLoginUnavailable() }, modifier = Modifier.weight(1f))
                SocialLoginButton("Apple",  onClick = { vm.showSocialLoginUnavailable() }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            AuthFooterText(
                prefix   = "Already have an account?",
                linkText = "Log in",
                onClick  = onNavigateLogin,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun SignUpPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        SignUpScreen(onNavigateVerify = {}, onNavigateLogin = {})
    }
}