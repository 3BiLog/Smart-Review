package com.example.smartreview.ui.screens.notifications.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartreview.ui.theme.*

@Composable
internal fun NotificationsTopBar(onMarkAllRead: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, Primary.copy(0.4f), CircleShape),
                ) {
                    AsyncImage(
                        model = "https://picsum.photos/seed/user/100/100",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    "SMART REVIEW",
                    style = MaterialTheme.typography.titleMedium.copy(
                        brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            IconButton(onClick = onMarkAllRead) {
                Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = Primary)
            }
        }
    }
}
