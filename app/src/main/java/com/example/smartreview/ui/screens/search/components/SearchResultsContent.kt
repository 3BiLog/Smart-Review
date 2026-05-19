package com.example.smartreview.ui.screens.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartreview.data.model.SearchResult
import com.example.smartreview.data.model.SortOption
import com.example.smartreview.ui.theme.*

@Composable
internal fun SearchResultsContent(
    results: List<SearchResult>,
    sortBy: SortOption,
    isSortMenuOpen: Boolean,
    onOpenSort: () -> Unit,
    onCloseSort: () -> Unit,
    onSortChange: (SortOption) -> Unit,
    onItemClick: (SearchResult) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SearchSortCountHeader(
                count = results.size,
                sortBy = sortBy,
                isSortMenuOpen = isSortMenuOpen,
                onOpenSort = onOpenSort,
                onCloseSort = onCloseSort,
                onSortChange = onSortChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        items(results, key = { it.id }) { result ->
            SearchResultItem(
                result = result,
                onClick = { onItemClick(result) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun SearchSortCountHeader(
    count: Int,
    sortBy: SortOption,
    isSortMenuOpen: Boolean,
    onOpenSort: () -> Unit,
    onCloseSort: () -> Unit,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            "$count kết quả",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface,
        )

        Box {
            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenSort),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        "Sắp xếp: ${sortBy.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = isSortMenuOpen,
                onDismissRequest = onCloseSort,
                modifier = Modifier.background(Surface),
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.label,
                                color = if (option == sortBy) Primary else OnSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClick = { onSortChange(option) },
                        leadingIcon = {
                            if (option == sortBy) {
                                Icon(Icons.Default.Check, null, tint = Primary, modifier = Modifier.size(16.dp))
                            }
                        },
                        colors = MenuDefaults.itemColors(textColor = OnSurface),
                    )
                }
            }
        }
    }
}

@Composable
internal fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                AsyncImage(
                    model = result.thumbnailUrl,
                    contentDescription = result.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.80f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                ) {
                    Text(
                        text = result.durationLabel,
                        color = SecondaryDim,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
                Surface(
                    color = GradientStart.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                ) {
                    Text(
                        text = result.category,
                        color = Primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = result.instructorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = result.formattedPrice,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (result.price == 0L) SecondaryDim else Primary,
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer)
                            .border(1.dp, GlassBorder, CircleShape),
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Tertiary, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "${result.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 10.sp,
                        )
                    }
                    Surface(
                        color = searchLevelBadgeColor(result.level).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = result.level,
                            color = searchLevelBadgeColor(result.level),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun searchLevelBadgeColor(level: String) = when (level) {
    "Beginner" -> SecondaryDim
    "Advanced" -> ErrorColor
    else -> Tertiary
}
