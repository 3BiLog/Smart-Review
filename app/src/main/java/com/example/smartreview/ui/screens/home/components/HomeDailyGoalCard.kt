package com.example.smartreview.ui.screens.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
            Text(
                "Mục tiêu hôm nay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
            )
            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    val radius = (size.minDimension - stroke) / 2f
                    val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
                    val arcSize = Size(radius * 2, radius * 2)
                    drawArc(
                        color = SurfaceVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                    drawArc(
                        brush = Brush.linearGradient(listOf(GradientStart, Secondary)),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$current",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface,
                    )
                    Text(
                        text = "/$target min",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Tiếp tục học để hoàn thành mục tiêu!",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
            )
        }
    }
}
