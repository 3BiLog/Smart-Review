package com.example.smartreview.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartreview.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR  – reused by both onboarding screens
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingTopBar(
    showSkip:      Boolean  = false,
    showAvatar:    Boolean  = false,
    showBackArrow: Boolean  = false,
    onBack:        () -> Unit = {},
    onSkip:        () -> Unit = {},
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Leading: back arrow or avatar
        if (showBackArrow) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurfaceVariant)
            }
        } else if (showAvatar) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary.copy(0.35f), CircleShape),
            ) {
                AsyncImage(
                    model              = "https://picsum.photos/seed/user/100/100",
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize(),
                )
            }
        } else {
            Spacer(Modifier.size(40.dp))
        }

        // Center: brand name
        Text(
            text  = "SMART REVIEW",
            style = MaterialTheme.typography.titleMedium.copy(
                brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                fontWeight = FontWeight.ExtraBold,
            ),
        )

        // Trailing: skip or notifications
        if (showSkip) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onSkip)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    "Skip",
                    style  = MaterialTheme.typography.labelLarge,
                    color  = OnSurfaceVariant,
                )
            }
        } else {
            Spacer(Modifier.size(40.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GRADIENT ONBOARDING BUTTON  – used by both screens
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GradientOnboardingButton(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text       = text,
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORB  – decorative background glow (no Modifier.blur needed)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BoxScope.AmbientOrb(
    size:    Dp,
    color:   Color,
    offsetX: Dp,
    offsetY: Dp,
    align:   Alignment,
) {
    Box(
        modifier = Modifier
            .size(size)
            .align(align)
            .offset(x = offsetX, y = offsetY)
            .background(
                Brush.radialGradient(colors = listOf(color, Color.Transparent)),
                CircleShape,
            )
    )
}