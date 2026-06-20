package com.example.smartreview.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.model.Course
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.theme.*

const val COURSES_LIST_ROUTE = "courses_list"

@Composable
fun CourseListScreen(
    navController: NavHostController,
    vm: CourseListViewModel = viewModel(),
) {
    val state  by vm.uiState.collectAsStateWithLifecycle()
    val focus   = LocalFocusManager.current

    Scaffold(
        containerColor = Background,
        topBar         = { CourseListTopBar() },
        bottomBar      = { SmartReviewBottomBar(navController) },
    ) { padding ->
        LazyVerticalGrid(
            columns              = GridCells.Fixed(2),
            contentPadding       = PaddingValues(
                start  = 16.dp, end   = 16.dp,
                top    = 12.dp, bottom = 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            modifier              = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item(span = { GridItemSpan(2) }) {
                CourseSearchBar(
                    query         = state.searchQuery,
                    onQueryChange = { vm.onSearchQueryChange(it) },
                    onSearch      = { focus.clearFocus() },
                )
            }

            item(span = { GridItemSpan(2) }) {
                FilterChipsRow(
                    filters        = state.filters,
                    selectedFilter = state.selectedFilter,
                    onFilterSelect = { vm.onFilterSelected(it) },
                )
            }

            if (state.filteredCourses.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    ) {
                        Text(
                            "Không tìm thấy khóa học",
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            items(
                items = state.filteredCourses,
                key   = { it.id },
            ) { course ->
                CourseGridCard(
                    course  = course,
                    onClick = {
                        navController.navigate("course_detail/${course.id}")
                    },
                )
            }
        }
    }
}

@Composable
private fun CourseListTopBar() {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                "Khóa học",
                style = MaterialTheme.typography.titleLarge.copy(
                    brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold,
                ),
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Primary)
            }
        }
    }
}

@Composable
private fun CourseSearchBar(
    query:         String,
    onQueryChange: (String) -> Unit,
    onSearch:      () -> Unit,
) {
    OutlinedTextField(
        value          = query,
        onValueChange  = onQueryChange,
        placeholder    = { Text("Tìm kiếm khóa học...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon    = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
        singleLine     = true,
        shape          = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Primary,
            unfocusedBorderColor    = GlassBorder,
            cursorColor             = Primary,
            focusedTextColor        = OnSurface,
            unfocusedTextColor      = OnSurface,
            focusedContainerColor   = SurfaceContainer,
            unfocusedContainerColor = SurfaceContainer,
            focusedPlaceholderColor = OnSurfaceVariant,
            unfocusedPlaceholderColor = OnSurfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FilterChipsRow(
    filters:        List<String>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(vertical = 4.dp),
    ) {
        items(filters) { filter ->
            val selected = filter == selectedFilter
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (selected)
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        else
                            Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainer))
                    )
                    .then(
                        if (!selected) Modifier.border(1.dp, GlassBorder, RoundedCornerShape(50.dp))
                        else Modifier
                    )
                    .clickable { onFilterSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text       = filter,
                    color      = if (selected) Color.White else OnSurfaceVariant,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun CourseGridCard(course: Course, onClick: () -> Unit) {
    val difficultyColor = difficultyColor(course.difficulty)

    Surface(
        color    = Surface,
        shape    = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            ) {
                AsyncImage(
                    model              = course.imageUrl,
                    contentDescription = course.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize(),
                )

                Surface(
                    color  = SurfaceContainer.copy(alpha = 0.85f),
                    shape  = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                ) {
                    Text(
                        text     = course.difficulty,
                        color    = difficultyColor,
                        style    = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                }

                Surface(
                    color  = Primary.copy(alpha = 0.20f),
                    shape  = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        Icon(Icons.Default.Bolt, null, tint = Primary, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "${course.xpReward} XP",
                            color = Primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text     = course.title,
                    style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color    = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, null, tint = OnSurfaceVariant, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${course.lessonCount} Bài học",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                    )
                }

                if (course.progress > 0f) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier              = Modifier.fillMaxWidth(),
                        ) {
                            Text("Tiến độ", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text(
                                "${(course.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                            )
                        }
                        LinearProgressIndicator(
                            progress     = { course.progress },
                            modifier     = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color        = Primary,
                            trackColor   = SurfaceVariant,
                            strokeCap    = StrokeCap.Round,
                        )
                    }
                } else {
                    Text(
                        if (course.price == 0L) "• Miễn phí" else "• ${course.formattedPrice}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (course.price == 0L) SecondaryDim else Tertiary,
                    )
                }
            }
        }
    }
}

internal fun difficultyColor(difficulty: String): androidx.compose.ui.graphics.Color =
    when (difficulty) {
        "Beginner"     -> SecondaryDim
        "Intermediate" -> Tertiary
        else           -> ErrorColor
    }