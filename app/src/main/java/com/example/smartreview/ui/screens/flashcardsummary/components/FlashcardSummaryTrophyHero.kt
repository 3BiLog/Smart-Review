package com.example.smartreview.ui.screens.flashcardsummary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.theme.*

@Composable
internal fun FlashcardSummaryTrophyHero(xpEarned: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(128.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 0.35f), Color.Transparent),
                        ),
                        shape = CircleShape,
                    )
                    .blur(32.dp),
            )

            Surface(
                shape = CircleShape,
                color = GlassBg,
                modifier = Modifier
                    .size(112.dp)
                    .border(1.dp, GlassBorder, CircleShape),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Trophy",
                        tint = Tertiary,
                        modifier = Modifier.size(58.dp),
                    )
                }
            }

            ConfettiDot(color = Secondary, size = 14.dp, offsetX = 62.dp, offsetY = (-8).dp)
            ConfettiDot(color = Primary, size = 10.dp, offsetX = (-68).dp, offsetY = (-10).dp)
            ConfettiDot(color = Tertiary, size = 8.dp, offsetX = 50.dp, offsetY = 56.dp)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Tuyệt vời!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
            ),
        )

        Spacer(Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = SurfaceVariant,
            modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(50.dp)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "XP",
                    tint = Secondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "+$xpEarned XP",
                    color = Secondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.ConfettiDot(color: Color, size: Dp, offsetX: Dp, offsetY: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .align(Alignment.Center)
            .offset(x = offsetX, y = offsetY)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.85f))
            .blur(1.dp),
    )
}
