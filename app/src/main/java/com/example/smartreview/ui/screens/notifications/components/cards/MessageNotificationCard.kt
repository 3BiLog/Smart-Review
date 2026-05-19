package com.example.smartreview.ui.screens.notifications.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartreview.ui.screens.notifications.NotificationItem
import com.example.smartreview.ui.theme.*

@Composable
internal fun MessageNotificationCard(item: NotificationItem, onClick: () -> Unit) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
        ) {
            Box(modifier = Modifier.size(52.dp)) {
                AsyncImage(
                    model = item.avatarUrl ?: "https://picsum.photos/seed/default/80/80",
                    contentDescription = item.senderName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, Primary.copy(0.35f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(if (item.isAvatarOnline) Secondary else OnSurfaceVariant)
                        .border(2.dp, Background, CircleShape),
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
                        text = item.senderName ?: item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = OnSurface,
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
                    color = if (!item.isRead) OnSurface else OnSurfaceVariant,
                    fontWeight = if (!item.isRead) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
