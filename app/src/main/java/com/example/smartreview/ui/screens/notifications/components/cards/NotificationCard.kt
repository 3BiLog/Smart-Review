package com.example.smartreview.ui.screens.notifications.components.cards

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.example.smartreview.ui.screens.notifications.NotificationItem
import com.example.smartreview.ui.screens.notifications.NotificationType
import com.example.smartreview.ui.theme.OnSurface
import com.example.smartreview.ui.theme.OnSurfaceVariant
import com.example.smartreview.ui.theme.Primary
import com.example.smartreview.ui.theme.Secondary
import com.example.smartreview.ui.theme.SurfaceVariant
import com.example.smartreview.ui.theme.Tertiary

@Composable
fun NotificationCard(item: NotificationItem, onClick: () -> Unit) {
    when (item.type) {
        NotificationType.PROMOTIONAL -> PromotionalNotificationCard(item, onClick)
        NotificationType.MESSAGE -> MessageNotificationCard(item, onClick)
        NotificationType.SYSTEM -> StandardNotificationCard(
            item = item,
            iconVector = Icons.Default.Settings,
            iconTint = OnSurfaceVariant,
            iconBgColor = SurfaceVariant,
            titleColor = OnSurfaceVariant,
            cardAlpha = 0.70f,
            onClick = onClick,
        )
        NotificationType.CHALLENGE -> StandardNotificationCard(
            item = item,
            iconVector = Icons.Default.Star,
            iconTint = Secondary,
            iconBgColor = Secondary.copy(0.15f),
            titleColor = Primary,
            isPriority = true,
            priorityColor = Secondary,
            showProgress = true,
            onClick = onClick,
        )
        NotificationType.COURSE_UPDATE -> StandardNotificationCard(
            item = item,
            iconVector = Icons.Default.School,
            iconTint = Primary,
            iconBgColor = Primary.copy(0.12f),
            titleColor = OnSurface,
            onClick = onClick,
        )
        NotificationType.COMMUNITY -> StandardNotificationCard(
            item = item,
            iconVector = Icons.Default.Group,
            iconTint = Tertiary,
            iconBgColor = Tertiary.copy(0.15f),
            titleColor = OnSurface,
            onClick = onClick,
        )
    }
}
