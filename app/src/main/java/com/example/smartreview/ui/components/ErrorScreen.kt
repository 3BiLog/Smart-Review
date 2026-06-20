package com.example.smartreview.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.theme.*

@Composable
fun ErrorScreen(
    modifier:        Modifier  = Modifier,
    title:           String    = "Something went wrong",
    description:     String    = "We couldn't load the content right now. Please check your connection and try again.",
    retryLabel:      String    = "Retry",
    returnLabel:     String    = "Return to Dashboard",
    icon:            ImageVector = Icons.Default.ErrorOutline,
    iconTint:        Color     = ErrorColor,
    onRetry:         (() -> Unit)? = null,
    onReturn:        (() -> Unit)? = null,
    illustration:    (@Composable () -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .fillMaxSize()
            .background(Background),
    ) {
        Box(
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-120).dp)
                .background(
                    Brush.radialGradient(listOf(GradientStart.copy(0.18f), Color.Transparent)),
                    CircleShape,
                )
        )

        Surface(
            color    = GlassBg,
            shape    = RoundedCornerShape(20.dp),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 24.dp)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .shadow(
                    elevation    = 0.dp,
                    shape        = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(0.30f),
                    spotColor    = Color.Black.copy(0.30f),
                ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier            = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            ) {
                if (illustration != null) {
                    illustration()
                } else {
                    ErrorIllustration(icon = icon, tint = iconTint)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text       = title,
                    style      = MaterialTheme.typography.headlineSmall,
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

                if (onRetry != null) {
                    GlowGradientButton(
                        text     = retryLabel,
                        icon     = Icons.Default.Refresh,
                        onClick  = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (onReturn != null) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(0.05f))
                            .border(2.dp, Primary.copy(0.25f), RoundedCornerShape(50.dp))
                            .clickable(onClick = onReturn)
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                    ) {
                        Text(
                            returnLabel,
                            style  = MaterialTheme.typography.labelMedium,
                            color  = Primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorIllustration(icon: ImageVector, tint: Color) {
    // Float animation
    val floatTransition = rememberInfiniteTransition(label = "errorFloat")
    val floatY by floatTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -10f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatY",
    )

    val glowAlpha by floatTransition.animateFloat(
        initialValue  = 0.20f,
        targetValue   = 0.50f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .size(180.dp)
            .graphicsLayer { translationY = floatY },
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    Brush.radialGradient(listOf(GradientStart.copy(glowAlpha), Color.Transparent)),
                    CircleShape,
                )
        )

        Surface(
            color    = GlassBg,
            shape    = CircleShape,
            modifier = Modifier
                .size(150.dp)
                .border(1.dp, GlassBorder, CircleShape),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier.fillMaxSize(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(54.dp))

                    Spacer(Modifier.height(10.dp))

                    listOf(
                        GradientStart to 44.dp,
                        Secondary     to 28.dp,
                        tint          to 16.dp,
                    ).forEach { (color, width) ->
                        Box(
                            modifier = Modifier
                                .width(width)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color.copy(0.70f))
                        )
                        Spacer(Modifier.height(3.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun GlowGradientButton(
    text:     String,
    icon:     ImageVector,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 2.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GradientStart.copy(alpha = 0.35f)),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                .clickable(onClick = onClick),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}


@Preview(
    name        = "ErrorScreen – Default",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    widthDp     = 390,
    heightDp    = 844,
)
@Composable
private fun ErrorScreenDefaultPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        ErrorScreen(
            onRetry  = {},
            onReturn = {},
        )
    }
}

@Preview(
    name        = "ErrorScreen – Network Error",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    widthDp     = 390,
    heightDp    = 844,
)
@Composable
private fun ErrorScreenNetworkPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        ErrorScreen(
            title       = "Network Error",
            description = "Unable to reach the server. Please check your internet connection.",
            icon        = Icons.Default.WifiOff,
            iconTint    = ErrorColor,
            onRetry     = {},
        )
    }
}

@Preview(
    name        = "ErrorScreen – No Actions",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    widthDp     = 390,
    heightDp    = 844,
)
@Composable
private fun ErrorScreenNoActionsPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        ErrorScreen()  // both callbacks null
    }
}