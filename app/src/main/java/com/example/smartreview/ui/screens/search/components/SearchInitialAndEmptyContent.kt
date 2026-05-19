package com.example.smartreview.ui.screens.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchInitialContent(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            "🔥 Tìm kiếm phổ biến",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { suggestion ->
                Surface(
                    color = SurfaceContainer,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .border(1.dp, GlassBorder, RoundedCornerShape(50.dp))
                        .clickable { onSuggestion(suggestion) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            suggestion,
                            color = OnSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SearchEmptyState(query: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(listOf(Primary.copy(0.20f), Color.Transparent)),
                        CircleShape,
                    ),
            )
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Không tìm thấy kết quả",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Không có kết quả nào phù hợp với\n\"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
