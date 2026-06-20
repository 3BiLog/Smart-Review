package com.example.smartreview.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartreview.ui.theme.*

@Composable
fun OnboardingFreeCoursesScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    // Entrance fade-in
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOut),
        label         = "fadeIn",
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .alpha(alpha),
    ) {

        AmbientOrb(
            size    = 300.dp,
            color   = GradientStart.copy(alpha = 0.20f),
            offsetX = 100.dp,
            offsetY = (-80).dp,
            align   = Alignment.TopEnd,
        )
        AmbientOrb(
            size    = 380.dp,
            color   = Secondary.copy(alpha = 0.08f),
            offsetX = (-60).dp,
            offsetY = 80.dp,
            align   = Alignment.BottomStart,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            OnboardingTopBar(
                showSkip  = true,
                onSkip    = onSkip,
            )

            Spacer(Modifier.weight(0.5f))

            IllustrationCard(
                imageUrl  = "https://picsum.photos/seed/onboard1/320/400",
                glowColor = GradientStart.copy(alpha = 0.30f),
                modifier  = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.weight(0.5f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(horizontal = 32.dp),
            ) {
                Text(
                    text      = "Numerous free trial courses",
                    style     = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color     = OnSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text      = "Dive into a massive library of bite-sized lessons. Master new skills through gamified micro-learning sessions designed for your busy schedule.",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                )
            }

            Spacer(Modifier.height(32.dp))

            OnboardingDots(
                currentPage = 0,
                totalPages  = 4,
            )

            Spacer(Modifier.height(28.dp))

            GradientOnboardingButton(
                text     = "Next Step",
                onClick  = onNext,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun IllustrationCard(
    imageUrl:  String,
    glowColor: Color,
    modifier:  Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(
                    Brush.radialGradient(colors = listOf(glowColor, Color.Transparent)),
                    CircleShape,
                )
        )

        Surface(
            color    = GlassBg,
            shape    = RoundedCornerShape(32.dp),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .aspectRatio(4f / 5f)
                .border(1.dp, GlassBorder, RoundedCornerShape(32.dp)),
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                AsyncImage(
                    model              = imageUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                )
            }
        }
    }
}

@Composable
fun OnboardingDots(currentPage: Int, totalPages: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = modifier,
    ) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue   = if (isActive) 32.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
                label = "dotWidth$index",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isActive)
                            Brush.linearGradient(listOf(GradientStart, Secondary))
                        else
                            Brush.linearGradient(listOf(SurfaceVariant, SurfaceVariant))
                    )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingFreeCoursesPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        OnboardingFreeCoursesScreen(onNext = {}, onSkip = {})
    }
}