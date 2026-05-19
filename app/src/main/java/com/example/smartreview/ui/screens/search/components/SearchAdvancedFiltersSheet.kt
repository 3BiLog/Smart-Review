package com.example.smartreview.ui.screens.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.screens.search.SearchUiState
import com.example.smartreview.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchAdvancedFiltersSheet(
    state: SearchUiState,
    onPriceChange: (Long) -> Unit,
    onRatingChange: (Float) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "Bộ lọc nâng cao",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
            TextButton(onClick = onReset) {
                Text("Đặt lại", color = Secondary, style = MaterialTheme.typography.labelLarge)
            }
        }

        HorizontalDivider(color = GlassBorder)

        SearchFilterSection(title = "Danh mục") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.filters.drop(1).forEach { cat ->
                    val selected = state.selectedFilter == cat
                    Surface(
                        color = if (selected) Primary.copy(0.20f) else SurfaceContainer,
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.border(
                            1.dp,
                            if (selected) Primary.copy(0.5f) else GlassBorder,
                            RoundedCornerShape(50.dp),
                        ),
                    ) {
                        Text(
                            text = cat,
                            color = if (selected) Primary else OnSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = GlassBorder)

        SearchFilterSection(title = "Giá tối đa") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("0đ", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Text(
                    if (state.filterPriceUpper >= state.filterMaxPrice) "Tất cả"
                    else "%,dđ".format(state.filterPriceUpper),
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(4.dp))

            Slider(
                value = state.filterPriceUpper.toFloat(),
                onValueChange = { onPriceChange(it.toLong()) },
                valueRange = 0f..state.filterMaxPrice.toFloat(),
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = SurfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0L to "Miễn phí", 500_000L to "≤500k", 1_000_000L to "≤1tr", 2_000_000L to "Tất cả")
                    .forEach { (value, label) ->
                        val selected = state.filterPriceUpper == value ||
                            (value == 2_000_000L && state.filterPriceUpper >= state.filterMaxPrice)
                        Surface(
                            color = if (selected) GradientStart.copy(0.20f) else SurfaceContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (selected) GradientStart.copy(0.5f) else GlassBorder,
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable { onPriceChange(value) },
                        ) {
                            Text(
                                label,
                                color = if (selected) Primary else OnSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
            }
        }

        HorizontalDivider(color = GlassBorder)

        SearchFilterSection(title = "Đánh giá tối thiểu") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(5) { idx ->
                    val starValue = idx + 1f
                    val filled = starValue <= state.filterMinRating
                    Icon(
                        imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (filled) Tertiary else OnSurfaceVariant,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onRatingChange(starValue) },
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.filterMinRating == 0f) "Tất cả" else "${state.filterMinRating}★ trở lên",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                .clickable(onClick = onApply),
        ) {
            Text(
                "Áp dụng bộ lọc",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SearchFilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
        )
        content()
    }
}
