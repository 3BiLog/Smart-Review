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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartreview.ui.theme.*

@Composable
fun AuthSuccessScreen(onDone: () -> Unit) {

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = EaseOut),
        label         = "successFadeIn",
    )
    // Scale-in for check circle
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "circleScale",
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .alpha(alpha),
    ) {
        // Background image (low opacity)
        AsyncImage(
            model              = "https://picsum.photos/seed/success_bg/800/1200",
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize().alpha(0.10f),
        )

        AuthBackgroundOrbs()

        // Simplified top bar
        Surface(
            color    = GlassBg,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .systemBarsPadding(),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    "SMART REVIEW",
                    style = MaterialTheme.typography.titleMedium.copy(
                        brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                        fontWeight = FontWeight.Bold,
                    ),
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.HelpOutline, null, tint = OnSurfaceVariant)
                }
            }
        }

        // Main card (centered)
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        ) {
            Surface(
                color    = GlassBg,
                shape    = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier            = Modifier.padding(32.dp),
                ) {

                    // ── Animated check circle ─────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .size(120.dp)
                            .then(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }),
                    ) {
                        // Glow halo
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    Brush.radialGradient(listOf(GradientStart.copy(0.35f), Color.Transparent)),
                                    CircleShape,
                                )
                        )
                        // Gradient circle
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier
                                    .size(52.dp)
                                    .align(Alignment.Center),
                            )
                        }
                    }

                    // ── "Success" gradient text ───────────────────────────
                    Text(
                        text  = "Success",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                            fontWeight = FontWeight.Bold,
                        ),
                    )

                    Text(
                        text      = "Congratulations, you have completed your registration!",
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(4.dp))

                    // ── Done button ───────────────────────────────────────
                    GradientAuthButton(
                        text     = "Done",
                        onClick  = onDone,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // ── Divider + profile ─────────────────────────────────
                    HorizontalDivider(color = GlassBorder)

                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(2.dp, Primary.copy(0.4f), CircleShape),
                        ) {
                            AsyncImage(
                                model              = "https://picsum.photos/seed/done_user/80/80",
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize(),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Setup complete for Guest User",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant,
                        )
                    }
                }
            }

            // ── Decorative corner chips ───────────────────────────────────
            Surface(
                color    = GlassBg,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-16).dp)
                    .rotate(12f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.Celebration, null, tint = Primary, modifier = Modifier.size(22.dp))
                }
            }
            Surface(
                color    = GlassBg,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-16).dp, y = 16.dp)
                    .rotate(-12f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.Verified, null, tint = Secondary, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

// graphicsLayer helper extension
private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier =
    this.then(Modifier.graphicsLayer(block))

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun AuthSuccessPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        AuthSuccessScreen(onDone = {})
    }
}