package com.example.smartreview.ui.screens.notifications.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.screens.notifications.NotificationItem
import com.example.smartreview.ui.theme.*

@Composable
internal fun StandardNotificationCard(
    item: NotificationItem,
    iconVector: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    titleColor: Color,
    cardAlpha: Float = 1f,
    isPriority: Boolean = false,
    priorityColor: Color = Secondary,
    showProgress: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .then(
                if (!item.isRead) {
                    Modifier.border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(GradientStart.copy(0.5f), GradientEnd.copy(0.3f)),
                            ),
                        ),
                        RoundedCornerShape(16.dp),
                    )
                } else {
                    Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                },
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(14.dp),
        ) {
            if (isPriority) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(priorityColor),
                )
                Spacer(Modifier.width(10.dp))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = titleColor,
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                            )
                        }
                        Text(
                            item.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (showProgress && item.progress != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = { item.progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Secondary,
                            trackColor = SurfaceVariant,
                        )
                        Text(
                            "${(item.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
