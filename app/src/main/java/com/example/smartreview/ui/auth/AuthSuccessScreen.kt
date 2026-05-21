package com.example.smartreview.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartreview.ui.theme.*

@Composable
fun AuthSuccessScreen(
    onDone: () -> Unit,
    vm: AuthViewModel,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val displayName = state.fullName.ifBlank { state.currentUserEmail.orEmpty() }
    val email = state.currentUserEmail.orEmpty()
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = EaseOut),
        label = "successFadeIn",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "circleScale",
    )
    LaunchedEffect(Unit) { visible = true }

    val resolvedName = displayName.trim().ifBlank {
        email.substringBefore("@").ifBlank { "SmartReview User" }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .alpha(alpha),
    ) {
        AuthBackgroundOrbs()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            AuthBrandSection(subtitle = "Tài khoản của bạn đã sẵn sàng")

            Spacer(Modifier.height(32.dp))

            AuthFormCard {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(96.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(GradientStart.copy(0.35f), Color.Transparent),
                                    ),
                                    CircleShape,
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(44.dp)
                                    .align(Alignment.Center),
                            )
                        }
                    }
                }

                Text(
                    text = "Đăng ký thành công!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "Chào mừng $resolvedName. Hồ sơ Firestore và phiên đăng nhập đã được thiết lập.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                if (email.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Mail, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(email, style = MaterialTheme.typography.labelMedium, color = OnSurface)
                    }
                }

                GradientAuthButton(
                    text = "Bắt đầu học ngay",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun AuthSuccessPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        // Preview uses a lightweight stub — real screen reads [AuthViewModel] state.
        AuthSuccessScreen(onDone = {}, vm = AuthViewModel())
    }
}
