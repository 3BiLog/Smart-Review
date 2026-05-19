package com.example.smartreview.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartreview.ui.screens.home.CourseCard
import com.example.smartreview.ui.theme.*

@Composable
internal fun HomeContinueCourseCard(card: CourseCard, onClick: () -> Unit) {
    Surface(
        color = Surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
    ) {
        Column {
            Box(modifier = Modifier.height(120.dp)) {
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = card.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    color = GlassBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                ) {
                    Text(
                        text = card.timeLeft,
                        color = Secondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { card.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Primary,
                        trackColor = SurfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${(card.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                }
            }
        }
    }
}
