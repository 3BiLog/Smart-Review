package com.example.smartreview.ui.screens.notifications.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.screens.notifications.NotificationTab
import com.example.smartreview.ui.theme.*

@Composable
internal fun NotificationTabBar(
    selectedTab: NotificationTab,
    unreadNotifications: Int,
    unreadMessages: Int,
    onSelect: (NotificationTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            NotificationTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val unreadCount = if (tab == NotificationTab.NOTIFICATIONS) unreadNotifications
                else unreadMessages

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                        )
                        .clickable { onSelect(tab) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            tab.label,
                            color = if (selected) Color.White else OnSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                        if (unreadCount > 0) {
                            Surface(
                                color = if (selected) Color.White.copy(0.25f) else Primary.copy(0.20f),
                                shape = CircleShape,
                            ) {
                                Text(
                                    "$unreadCount",
                                    color = if (selected) Color.White else Primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
