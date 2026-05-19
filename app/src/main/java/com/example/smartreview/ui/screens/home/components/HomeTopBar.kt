package com.example.smartreview.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartreview.ui.theme.*

@Composable
internal fun HomeTopBar(userName: String, xp: Int, streak: Int) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant),
                ) {
                    AsyncImage(
                        model = "https://picsum.photos/seed/avatar/100/100",
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SMART REVIEW",
                        style = MaterialTheme.typography.titleMedium.copy(
                            brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "Hi, $userName! 👋",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(icon = Icons.Default.LocalFireDepartment, value = "$streak", tint = Tertiary)
                StatChip(icon = Icons.Default.Bolt, value = "$xp", tint = Secondary)
            }
        }
    }
}

@Composable
private fun StatChip(icon: ImageVector, value: String, tint: Color) {
    Surface(
        color = SurfaceVariant,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, color = OnSurface)
        }
    }
}
