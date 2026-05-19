package com.example.smartreview.ui.screens.flashcardsummary.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.theme.GlassBg
import com.example.smartreview.ui.theme.GlassBorder
import com.example.smartreview.ui.theme.OnSurface
import com.example.smartreview.ui.theme.OnSurfaceVariant

@Composable
internal fun FlashcardSummaryStatTile(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .height(130.dp)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                )
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                )
            }
        }
    }
}
