package com.example.smartreview.ui.screens.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.theme.*

@Composable
internal fun SearchFilterChips(
    filters: List<String>,
    selectedFilter: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters) { filter ->
            val selected = filter == selectedFilter
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (selected)
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        else
                            Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainer)),
                    )
                    .then(
                        if (!selected) Modifier.border(1.dp, GlassBorder, RoundedCornerShape(50.dp))
                        else Modifier,
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Text(
                    text = filter,
                    color = if (selected) Color.White else OnSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
