package com.example.smartreview.ui.screens.flashcardsummary.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.theme.*

@Composable
internal fun FlashcardSummaryAccuracyTile(
    progress: Float,
    accuracy: Float,
    knownCount: Int,
    reviewCount: Int,
    modifier: Modifier = Modifier,
) {
    val pct = (accuracy * 100).toInt()

    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(20.dp),
        ) {
            Column {
                Text(
                    text = "Độ chính xác",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$knownCount đã thuộc, $reviewCount cần ôn",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                )
            }

            FlashcardSummaryAccuracyRing(
                progress = progress,
                modifier = Modifier.size(96.dp),
            )
        }
    }
}

@Composable
private fun FlashcardSummaryAccuracyRing(progress: Float, modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val topLeft = Offset(
                x = (size.width - radius * 2f) / 2f,
                y = (size.height - radius * 2f) / 2f,
            )
            val arcSize = Size(radius * 2f, radius * 2f)
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            drawArc(
                color = SurfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                topLeft = topLeft,
                size = arcSize,
            )

            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(GradientStart, Secondary),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = stroke,
                topLeft = topLeft,
                size = arcSize,
            )
        }

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Accuracy",
            tint = Secondary,
            modifier = Modifier.size(32.dp),
        )
    }
}
