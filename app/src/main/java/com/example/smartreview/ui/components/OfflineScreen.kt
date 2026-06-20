package com.example.smartreview.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
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
import com.example.smartreview.ui.theme.*

@Composable
fun OfflineScreen(
    onRetry:           () -> Unit,
    modifier:          Modifier           = Modifier,
    title:             String             = "Mất kết nối internet",
    description:       String             = "Vui lòng kiểm tra lại đường truyền mạng của bạn để tiếp tục trải nghiệm học tập.",
    retryLabel:        String             = "Thử lại",
    backgroundContent: (@Composable () -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier.fillMaxSize(),
    ) {
        if (backgroundContent != null) {
            Box(modifier = Modifier.fillMaxSize().alpha(0.40f)) {
                backgroundContent()
            }
        } else {
            SkeletonBackground()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background.copy(alpha = 0.60f)),
        )

        OfflineCard(
            title       = title,
            description = description,
            retryLabel  = retryLabel,
            onRetry     = onRetry,
            modifier    = Modifier
                .widthIn(max = 380.dp)
                .padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun OfflineCard(
    title:       String,
    description: String,
    retryLabel:  String,
    onRetry:     () -> Unit,
    modifier:    Modifier = Modifier,
) {
    // Icon pulse animation
    val pulseTransition = rememberInfiniteTransition(label = "wifiPulse")
    val iconScale by pulseTransition.animateFloat(
        initialValue  = 0.90f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "iconScale",
    )
    val glowAlpha by pulseTransition.animateFloat(
        initialValue  = 0.10f,
        targetValue   = 0.30f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Box {
            // Top ambient glow inside card
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-120).dp)
                    .background(
                        Brush.radialGradient(listOf(Primary.copy(glowAlpha), Color.Transparent)),
                        CircleShape,
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
            ) {

                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(90.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                            .background(
                                Brush.radialGradient(listOf(Primary.copy(glowAlpha), Color.Transparent)),
                                CircleShape,
                            )
                    )
                    Surface(
                        color    = SurfaceContainer,
                        shape    = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .border(2.dp, GlassBorder, CircleShape),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector        = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint               = OnSurfaceVariant,
                                modifier           = Modifier.size(38.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface,
                    textAlign  = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text      = description,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                GlowGradientButton(
                    text     = retryLabel,
                    icon     = Icons.Default.Refresh,
                    onClick  = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SkeletonBackground() {
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by shimmer.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
            .alpha(shimmerAlpha),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBox(modifier = Modifier.size(40.dp).clip(CircleShape))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonBox(modifier = Modifier.width(110.dp).height(14.dp).clip(RoundedCornerShape(4.dp)))
                    SkeletonBox(modifier = Modifier.width(70.dp).height(10.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
            SkeletonBox(modifier = Modifier.size(40.dp).clip(CircleShape))
        }

        Spacer(Modifier.height(8.dp))

        SkeletonBox(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp)))

        SkeletonBox(modifier = Modifier.width(140.dp).height(18.dp).clip(RoundedCornerShape(4.dp)))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SkeletonBox(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(16.dp)))
            SkeletonBox(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(16.dp)))
        }

        SkeletonBox(modifier = Modifier.width(100.dp).height(18.dp).clip(RoundedCornerShape(4.dp)))

        repeat(3) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(16.dp)))
        }
    }
}

@Composable
private fun SkeletonBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(SurfaceContainer))
}


@Preview(
    name        = "OfflineScreen – Standalone",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    widthDp     = 390,
    heightDp    = 844,
)
@Composable
private fun OfflineScreenStandalonePreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        OfflineScreen(onRetry = {})
    }
}

@Preview(
    name        = "OfflineScreen – Custom text",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    widthDp     = 390,
    heightDp    = 844,
)
@Composable
private fun OfflineScreenCustomPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        OfflineScreen(
            onRetry     = {},
            title       = "No Internet Connection",
            description = "Please turn on Wi-Fi or mobile data and try again.",
            retryLabel  = "Reconnect",
        )
    }
}