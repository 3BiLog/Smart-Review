package com.example.smartreview.ui.screens.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.theme.OnSurface
import com.example.smartreview.ui.theme.OnSurfaceVariant
import com.example.smartreview.ui.theme.Primary

@Composable
internal fun NotificationsEmptyState(isMessages: Boolean, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(88.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        Brush.radialGradient(listOf(Primary.copy(0.15f), Color.Transparent)),
                        CircleShape,
                    ),
            )
            Icon(
                imageVector = if (isMessages) Icons.Default.MarkChatRead else Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(38.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (isMessages) "Chưa có tin nhắn nào" else "Bạn đã đọc hết thông báo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isMessages) "Các tin nhắn từ giảng viên và nhóm học sẽ xuất hiện ở đây."
            else "Tất cả thông báo mới sẽ xuất hiện ở đây.",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
