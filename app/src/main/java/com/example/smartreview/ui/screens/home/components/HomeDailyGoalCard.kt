package com.example.smartreview.ui.screens.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.theme.*

@Composable
internal fun HomeDailyGoalCard(
    progress: Float,
    current: Int,
    target: Int,
    xpEarned: Int = 0,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Mục tiêu hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                )
                if (isCompleted) {
                    Surface(
                        color = Secondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = Secondary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "+$xpEarned XP",
                                color = Secondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    val radius = (size.minDimension - stroke) / 2f
                    val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
                    val arcSize = Size(radius * 2, radius * 2)

                    // Background
                    drawArc(
                        color = SurfaceVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize,
                    )

                    // Progress
                    val sweepAngle = 360f * progress.coerceAtMost(1f)
                    drawArc(
                        brush = Brush.linearGradient(
                            if (isCompleted) listOf(Secondary, Secondary.copy(alpha = 0.7f))
                            else listOf(GradientStart, Secondary)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isCompleted) "✅" else "$current",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isCompleted) Secondary else OnSurface,
                    )
                    Text(
                        text = if (isCompleted) "Hoàn thành!" else "/$target min",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) Secondary else OnSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (isCompleted) "Bạn đã hoàn thành mục tiêu hôm nay!"
                else "Tiếp tục học để hoàn thành mục tiêu!",
                style = MaterialTheme.typography.bodySmall,
                color = if (isCompleted) Secondary else OnSurfaceVariant,
            )
        }
    }
}
