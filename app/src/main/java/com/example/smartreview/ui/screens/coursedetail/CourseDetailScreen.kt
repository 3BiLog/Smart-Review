package com.example.smartreview.ui.screens.coursedetail

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.ui.screens.courses.difficultyColor
import com.example.smartreview.ui.screens.lesson.lessonRoute
import com.example.smartreview.ui.theme.*

// ─── Route ───────────────────────────────────────────────────────────────────
const val COURSE_DETAIL_ROUTE = "course_detail/{courseId}"
fun courseDetailRoute(courseId: String) = "course_detail/$courseId"

// ─── Screen ──────────────────────────────────────────────────────────────────
@Composable
fun CourseDetailScreen(
    navController: NavHostController,
    courseId:      String,
    vm: CourseDetailViewModel = viewModel(
        factory = CourseDetailViewModel.provideFactory(courseId)
    ),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val course = state.course ?: return

    Scaffold(
        containerColor = Background,
        topBar = {
            CourseDetailTopBar(
                onBack  = { navController.popBackStack() },
                onShare = { /* share */ },
            )
        },
        bottomBar = {
            CourseDetailBottomBar(
                price        = course.formattedPrice,
                isBookmarked = state.isBookmarked,
                onBookmark   = { vm.toggleBookmark() },
                onBuy        = { /* navigate to payment */ },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {

            // ── 1. Hero video ─────────────────────────────────────────────
            item {
                HeroVideoSection(
                    imageUrl     = course.imageUrl,
                    isBestseller = course.isBestseller,
                    onPlay       = { navController.navigate("lesson_player/l1") },
                )
            }

            // ── 2. Course info ────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title + Price row
                    Row(
                        verticalAlignment     = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text       = course.title,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color      = OnSurface,
                            modifier   = Modifier.weight(1f).padding(end = 12.dp),
                        )
                        Text(
                            text       = course.formattedPrice,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Tertiary,
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Description
                    Text(
                        text     = course.description,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = OnSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(12.dp))

                    // Stats row
                    CourseStatsRow(
                        rating      = course.rating,
                        reviewCount = course.reviewCount,
                        duration    = "${course.durationHours}h",
                        lessonCount = course.lessonCount,
                    )
                }
            }

            // ── 3. Divider ────────────────────────────────────────────────
            item { HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 16.dp)) }

            // ── 4. Instructor ─────────────────────────────────────────────
            item {
                InstructorCard(
                    name       = course.instructorName,
                    title      = course.instructorTitle,
                    avatarUrl  = course.instructorAvatar,
                    onFollow   = {},
                    modifier   = Modifier.padding(16.dp),
                )
            }

            // ── 5. Divider ────────────────────────────────────────────────
            item { HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 16.dp)) }

            // ── 6. Course content ─────────────────────────────────────────
            item {
                Text(
                    "Course Content",
                    style    = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color    = OnSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            // ── 7. Modules ────────────────────────────────────────────────
            items(course.modules, key = { it.id }) { module ->
                ModuleCard(
                    module     = module,
                    isExpanded = module.id in state.expandedModuleIds,
                    onToggle   = { vm.toggleModule(module.id) },
                    onLessonClick = { lesson ->
                        if (!lesson.isLocked) {
                            navController.navigate(lessonRoute(lesson.id))
                        }
                    },
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO VIDEO SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroVideoSection(
    imageUrl:     String,
    isBestseller: Boolean,
    onPlay:       () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
    ) {
        // Thumbnail
        AsyncImage(
            model              = imageUrl,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(0.15f), Color.Black.copy(0.55f))
                    )
                )
        )

        // Play button
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(64.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(GlassBg)
                .border(1.dp, GlassBorder, CircleShape)
                .clickable(onClick = onPlay),
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint     = Primary,
                modifier = Modifier.size(34.dp),
            )
        }

        // BESTSELLER badge
        if (isBestseller) {
            Surface(
                color    = SecondaryDim.copy(alpha = 0.90f),
                shape    = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            ) {
                Text(
                    "BESTSELLER",
                    color    = Color(0xFF003828),
                    style    = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CourseStatsRow(
    rating:      Float,
    reviewCount: Int,
    duration:    String,
    lessonCount: Int,
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = Tertiary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("$rating", color = OnSurface, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(" ($reviewCount)", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }

        StatDot()

        // Duration
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(duration, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }

        StatDot()

        // Lessons
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Movie, null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("$lessonCount Lessons", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatDot() {
    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(OnSurfaceVariant.copy(0.4f)))
}

// ─────────────────────────────────────────────────────────────────────────────
// INSTRUCTOR CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun InstructorCard(
    name:      String,
    title:     String,
    avatarUrl: String,
    onFollow:  () -> Unit,
    modifier:  Modifier = Modifier,
) {
    Surface(
        color    = SurfaceContainer,
        shape    = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(12.dp),
        ) {
            AsyncImage(
                model              = avatarUrl,
                contentDescription = name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary.copy(0.4f), CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.SemiBold)
                Text(title, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
            OutlinedButton(
                onClick = onFollow,
                shape   = RoundedCornerShape(10.dp),
                border  = BorderStroke(2.dp, Primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text("Follow", color = Primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MODULE CARD (expandable)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ModuleCard(
    module:        CourseModule,
    isExpanded:    Boolean,
    onToggle:      () -> Unit,
    onLessonClick: (LessonItem) -> Unit,
    modifier:      Modifier = Modifier,
) {
    Surface(
        color    = if (module.isLocked) Surface.copy(alpha = 0.6f) else GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Column {
            // ── Module header ─────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = module.title,
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (module.isLocked) OnSurfaceVariant else Primary,
                    )
                    Text(
                        text  = module.durationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint     = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            // ── Lessons (animated) ────────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    module.lessons.forEach { lesson ->
                        LessonRow(
                            lesson  = lesson,
                            onClick = { onLessonClick(lesson) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: LessonItem, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(enabled = !lesson.isLocked, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(if (lesson.isLocked) Modifier.background(Color.Transparent) else Modifier),
    ) {
        // Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (lesson.isLocked) SurfaceVariant
                    else Primary.copy(0.12f)
                ),
        ) {
            Icon(
                imageVector        = if (lesson.isLocked) Icons.Default.Lock else Icons.Default.PlayCircle,
                contentDescription = null,
                tint               = if (lesson.isLocked) OnSurfaceVariant else Primary,
                modifier           = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text      = lesson.title,
                style     = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color     = if (lesson.isLocked) OnSurfaceVariant else OnSurface,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis,
            )
            if (!lesson.isLocked) {
                Text(lesson.formattedDuration, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CourseDetailBottomBar(
    price:        String,
    isBookmarked: Boolean,
    onBookmark:   () -> Unit,
    onBuy:        () -> Unit,
) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Bookmark button
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .clickable(onClick = onBookmark),
            ) {
                Icon(
                    imageVector        = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.Bookmark,
                    contentDescription = "Bookmark",
                    tint               = if (isBookmarked) Tertiary else OnSurface,
                    modifier           = Modifier.size(24.dp),
                )
            }

            // Buy / Start button
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                    .clickable(onClick = onBuy),
            ) {
                Text(
                    text  = if (price == "Miễn phí") "Bắt đầu học" else "Mua ngay – $price",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CourseDetailTopBar(onBack: () -> Unit, onShare: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Primary)
            }
            Text(
                "SMART REVIEW",
                style = MaterialTheme.typography.titleMedium.copy(
                    brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold,
                ),
            )
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, null, tint = OnSurfaceVariant)
            }
        }
    }
}