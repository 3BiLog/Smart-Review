package com.example.smartreview.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartreview.ui.theme.*

@Composable
fun OnboardingStudyPlanScreen(
    onSignUp: () -> Unit,
    onLogIn:  () -> Unit,
    onBack:   () -> Unit,
) {
    var progressAnimated by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue   = progressAnimated,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label         = "weeklyProgress",
    )
    LaunchedEffect(Unit) { progressAnimated = 0.84f }

    var visible by remember { mutableStateOf(false) }
    val screenAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = EaseOut),
        label         = "screenFadeIn",
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .alpha(screenAlpha),
    ) {

        AmbientOrb(size = 320.dp, color = GradientStart.copy(0.15f), offsetX = 100.dp, offsetY = (-60).dp, align = Alignment.TopEnd)
        AmbientOrb(size = 280.dp, color = Secondary.copy(0.08f),     offsetX = (-60).dp, offsetY = 60.dp, align = Alignment.BottomStart)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {

            OnboardingTopBar(
                showAvatar   = true,
                showBackArrow = true,
                onBack       = onBack,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {

                HeroImageCard(
                    imageUrl = "https://picsum.photos/seed/studyplan/600/300",
                    modifier = Modifier.fillMaxWidth(),
                )

                DescriptionCard(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                )

                AdaptiveLearningChip(modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(4.dp))

                GradientOnboardingButton(
                    text     = "Sign up",
                    onClick  = onSignUp,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedButton(
                    onClick  = onLogIn,
                    shape    = RoundedCornerShape(16.dp),
                    border   = BorderStroke(2.dp, Primary.copy(0.6f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text("Log in", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Text(
                    text      = "By continuing, you agree to our Terms of Service",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(16.dp))
            }

            StepIndicator(
                currentStep = 1,
                totalSteps  = 3,
                modifier    = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun HeroImageCard(imageUrl: String, modifier: Modifier = Modifier) {
    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
            .height(200.dp)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Box {
            AsyncImage(
                model              = imageUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .alpha(0.60f),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Background.copy(alpha = 0.55f),
                                Background.copy(alpha = 0.90f),
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            ) {
                Surface(
                    color    = GlassBg,
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(8.dp)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Icon(Icons.Default.TrendingUp, null, tint = Secondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Growth Engine",
                            style  = MaterialTheme.typography.labelMedium,
                            color  = Secondary,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Master Your Own Pace.",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                )
            }
        }
    }
}

@Composable
private fun DescriptionCard(progress: Float, modifier: Modifier = Modifier) {
    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Create your own study plan",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = Primary,
            )
            Text(
                "Harness the power of AI-driven analytics to build a curriculum that adapts to your daily schedule. Track your mastery with real-time data visualization and gamified milestones.",
                style  = MaterialTheme.typography.bodyMedium,
                color  = OnSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Weekly Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant,
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Secondary,
                    )
                }
                LinearProgressIndicator(
                    progress   = { progress },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color      = Secondary,
                    trackColor = SurfaceVariant,
                    strokeCap  = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun AdaptiveLearningChip(modifier: Modifier = Modifier) {
    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(0.12f)),
            ) {
                Icon(
                    Icons.Default.AutoGraph,
                    contentDescription = null,
                    tint               = Primary,
                    modifier           = Modifier.size(24.dp),
                )
            }
            Column {
                Text(
                    "Adaptive Learning",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = OnSurface,
                )
                Text(
                    "Plans adjust based on your performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = modifier,
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val width by animateDpAsState(
                targetValue   = if (isActive) 48.dp else 32.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label         = "stepWidth$index",
            )
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(width)
                    .clip(RoundedCornerShape(2.dp))
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
private fun OnboardingStudyPlanPreview() {
    com.example.smartreview.ui.theme.SmartReviewTheme {
        OnboardingStudyPlanScreen(onSignUp = {}, onLogIn = {}, onBack = {})
    }
}