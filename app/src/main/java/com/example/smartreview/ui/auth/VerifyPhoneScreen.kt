package com.example.smartreview.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartreview.ui.theme.*

@Composable
fun VerifyPhoneScreen(
    onVerified: () -> Unit,
    onBack:     () -> Unit,
    vm: VerifyPhoneViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Cursor blink animation
    val cursorTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label         = "cursorAlpha",
    )

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

            // ── Top bar ───────────────────────────────────────────────────
            VerifyTopBar(onBack = onBack)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(24.dp))

                // Phone + instruction
                Icon(
                    Icons.Default.Sms,
                    contentDescription = null,
                    tint     = Secondary,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text      = buildAnnotatedString {
                        append("Code is sent to ")
                        pushStyle(androidx.compose.ui.text.SpanStyle(color = Primary, fontWeight = FontWeight.Bold))
                        append(state.phoneNumber)
                        pop()
                    },
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(32.dp))

                // ── OTP display boxes ─────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    state.otpDigits.forEachIndexed { index, digit ->
                        val isActiveBox = digit.isEmpty() && index == state.filledCount
                        OtpBox(
                            digit      = digit,
                            isActive   = isActiveBox,
                            cursorAlpha = cursorAlpha,
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Verify button ─────────────────────────────────────────
                GradientAuthButton(
                    text     = "Verify and Create Account",
                    onClick  = { vm.onVerify(onVerified) },
                    enabled  = state.isComplete,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                // Error message
                state.error?.let { err ->
                    Text(err, color = ErrorColor, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                }

                // Resend link
                Row(horizontalArrangement = Arrangement.Center) {
                    Text("Didn't receive the code? ", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text(
                        "Resend",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = Secondary,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.clickable { vm.resendCode() },
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Numeric keypad ────────────────────────────────────────
                NumericKeypad(
                    onDigitPress    = { vm.pressDigit(it) },
                    onBackspace     = { vm.pressBackspace() },
                    onBiometric     = { /* biometric auth */ },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OTP BOX  – single digit display cell
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OtpBox(digit: String, isActive: Boolean, cursorAlpha: Float) {
    Surface(
        color    = SurfaceContainer,
        shape    = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(width = 64.dp, height = 80.dp)
            .border(
                2.dp,
                if (isActive) Primary.copy(0.6f) else if (digit.isNotEmpty()) Primary.copy(0.3f) else GlassBorder,
                RoundedCornerShape(16.dp),
            ),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (digit.isNotEmpty()) {
                Text(
                    text       = digit,
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Primary,
                )
            } else if (isActive) {
                // Blinking cursor
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .alpha(cursorAlpha)
                        .background(Primary),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NUMERIC KEYPAD
// ─────────────────────────────────────────────────────────────────────────────
private data class KeyData(
    val digit:     String  = "",
    val subLabel:  String  = "",
    val isSpecial: Boolean = false,
    val specialType: String = "",  // "fingerprint" | "backspace" | ""
)

@Composable
private fun NumericKeypad(
    onDigitPress: (String) -> Unit,
    onBackspace:  () -> Unit,
    onBiometric:  () -> Unit,
) {
    val rows = listOf(
        listOf(KeyData("1"), KeyData("2", "ABC"), KeyData("3", "DEF")),
        listOf(KeyData("4", "GHI"), KeyData("5", "JKL"), KeyData("6", "MNO")),
        listOf(KeyData("7", "PQRS"), KeyData("8", "TUV"), KeyData("9", "WXYZ")),
        listOf(
            KeyData(isSpecial = true, specialType = "fingerprint"),
            KeyData("0"),
            KeyData(isSpecial = true, specialType = "backspace"),
        ),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = Modifier.fillMaxWidth(),
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier              = Modifier.fillMaxWidth(),
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key         = key,
                        onClick     = {
                            when {
                                key.isSpecial && key.specialType == "backspace"    -> onBackspace()
                                key.isSpecial && key.specialType == "fingerprint"  -> onBiometric()
                                key.digit.isNotEmpty()                             -> onDigitPress(key.digit)
                            }
                        },
                        modifier    = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(key: KeyData, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isSpecial = key.isSpecial

    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (!isSpecial) SurfaceContainer else Color.Transparent)
            .then(if (!isSpecial) Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp)) else Modifier)
            .clickable(onClick = onClick),
    ) {
        when (key.specialType) {
            "fingerprint" -> Icon(Icons.Default.Fingerprint, null, tint = OnSurfaceVariant, modifier = Modifier.size(28.dp))
            "backspace"   -> Icon(Icons.Default.Backspace, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
            else          -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = key.digit,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = OnSurface,
                )
                if (key.subLabel.isNotEmpty()) {
                    Text(
                        text          = key.subLabel,
                        fontSize      = 8.sp,
                        letterSpacing = 2.sp,
                        color         = OnSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun VerifyTopBar(onBack: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Primary)
            }
            Text(
                "Verify Phone",
                style = MaterialTheme.typography.titleMedium.copy(
                    brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun VerifyPhonePreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        VerifyPhoneScreen(onVerified = {}, onBack = {})
    }
}