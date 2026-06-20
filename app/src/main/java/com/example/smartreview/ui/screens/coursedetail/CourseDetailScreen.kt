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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.ui.screens.courses.difficultyColor
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateHeroPlay
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateLessonVideo
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateStartLearning
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateReading
import com.example.smartreview.ui.screens.coursereviews.courseReviewsRoute
import com.example.smartreview.ui.screens.payment.PaymentRoutes
import com.example.smartreview.ui.screens.quiz.quizRoute
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.delay

const val COURSE_DETAIL_ROUTE = "course_detail/{courseId}?justPaid={justPaid}"

fun courseDetailRoute(courseId: String, justPaid: Boolean = false): String {
    return "course_detail/$courseId?justPaid=$justPaid"
}

@Composable
fun CourseDetailScreen(
    navController: NavHostController,
    courseId:      String,
    vm: CourseDetailViewModel = viewModel(
        factory = CourseDetailViewModel.provideFactory(courseId)
    ),
) {
    val justPaid = navController.currentBackStackEntry?.arguments?.getString("justPaid")?.toBoolean() ?: false
    android.util.Log.d("CourseDetailScreen", "✅ justPaid parsed from arguments: $justPaid")

    val state by vm.uiState.collectAsStateWithLifecycle()
    val course = state.course ?: return
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(justPaid) {
        android.util.Log.d("CourseDetailScreen", "🚀 LaunchedEffect triggered with justPaid=$justPaid")
        if (justPaid) {
            android.util.Log.d("CourseDetailScreen", "▶️ Calling vm.refreshEnrollment(justPaid=true)")
            vm.refreshEnrollment(justPaid = true)
            delay(500)
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshEnrollment()
                vm.refreshProgression()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

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
                isEnrolled   = state.isEnrolled || course.price == 0L,
                onBookmark   = { vm.toggleBookmark() },
                onBuy        = {
                    if (course.price == 0L || state.isEnrolled) {
                        navController.navigateStartLearning(
                            course = course,
                            preferredLessonId = state.recommendedNextLessonId,
                        )
                    } else {
                        navController.navigate(
                            PaymentRoutes.methodRoute(
                                courseId = course.id,
                                courseName = course.title,
                                coursePrice = course.price,
                            ),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {

            item {
                HeroVideoSection(
                    imageUrl     = course.imageUrl,
                    isBestseller = course.isBestseller,
                    onPlay       = {
                        navController.navigateHeroPlay(course = course)
                    },
                )
            }

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

                    Text(
                        text     = course.description,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = OnSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(12.dp))

                    CourseStatsRow(
                        rating      = course.rating,
                        reviewCount = course.reviewCount,
                        duration    = "${course.durationHours}h",
                        lessonCount = course.lessonCount,
                    )

                    if (state.totalLessonCount > 0) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { state.courseProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Primary,
                            trackColor = SurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tiến độ: ${state.completedLessonCount}/${state.totalLessonCount} bài hoàn thành",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                        )
                    }

                    state.recommendedNextLessonTitle?.let { nextTitle ->
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                navController.navigateStartLearning(
                                    course = course,
                                    preferredLessonId = state.recommendedNextLessonId,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Tiếp tục: $nextTitle", color = Primary)
                        }
                    }
                }
            }

            item { HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                CourseDetailReviewSection(
                    summary = state.reviewSummary,
                    userReview = state.userReview,
                    totalReviewCount = state.reviewSummary?.totalReviews ?: state.reviews.size,
                    isLoading = state.isLoadingReviews,
                    onWriteReview = { vm.showReviewDialog() },
                    onEditReview = { vm.showEditReviewDialog() },
                    onViewAllReviews = {
                        navController.navigate(courseReviewsRoute(course.id, course.title))
                    },
                    modifier = Modifier.padding(16.dp),
                )
            }

            item { HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                Text(
                    "Course Content",
                    style    = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color    = OnSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            items(course.modules, key = { it.id }) { module ->
                ModuleCard(
                    module     = module,
                    isExpanded = module.id in state.expandedModuleIds,
                    onToggle   = { vm.toggleModule(module.id) },
                    onLessonClick = { lesson ->
                        android.util.Log.d("CourseDetailScreen", "Lesson clicked: id=${lesson.id}, type=${lesson.lessonType}, isLocked=${lesson.isLocked}")
                        if (!lesson.isLocked) {
                            when (lesson.lessonType) {
                                LessonType.VIDEO, LessonType.UNKNOWN -> {
                                    android.util.Log.d("CourseDetailScreen", "Navigating to video: lesson=${lesson.id}, course=${course.id}")
                                    navController.navigateLessonVideo(lesson.id, courseId = course.id)
                                }
                                LessonType.READING -> navController.navigateReading(lesson.id)
                                LessonType.QUIZ -> navController.navigate(quizRoute(lesson.quizId ?: lesson.id))
                                LessonType.FLASHCARD -> navController.navigate("flashcard/${lesson.id}")
                            }
                        }
                    },
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
    if (state.showReviewDialog) {
        val isEditing = state.userReview != null
        AlertDialog(
            onDismissRequest = { vm.dismissReviewDialog() },
            title = {
                Text(
                    if (isEditing) "Sửa đánh giá" else "Viết đánh giá",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Đánh giá:", style = MaterialTheme.typography.bodyMedium)
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { vm.setRatingInput(star) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    if (star <= state.ratingInput) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star sao",
                                    tint = if (star <= state.ratingInput) Secondary else OnSurfaceVariant,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.reviewContentInput,
                        onValueChange = { vm.setReviewContentInput(it) },
                        label = { Text("Nội dung đánh giá") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = GlassBorder,
                        ),
                    )

                    state.reviewSubmitError?.let { error ->
                        Text(
                            error,
                            color = ErrorColor,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { vm.submitReview(state.ratingInput, state.reviewContentInput) },
                    enabled = state.ratingInput > 0 &&
                        state.reviewContentInput.isNotBlank() &&
                        !state.isSubmittingReview,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (state.isSubmittingReview) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(if (isEditing) "Cập nhật" else "Gửi đánh giá", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissReviewDialog() }) {
                    Text("Hủy", color = OnSurfaceVariant)
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(20.dp),
        )
    }
}

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
        AsyncImage(
            model              = imageUrl,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(0.15f), Color.Black.copy(0.55f))
                    )
                )
        )

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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = Tertiary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("$rating", color = OnSurface, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(" ($reviewCount)", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }

        StatDot()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(duration, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }

        StatDot()

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

@Composable
private fun CourseDetailBottomBar(
    price:        String,
    isBookmarked: Boolean,
    isEnrolled:   Boolean,
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
                    text  = when {
                        price == "Miễn phí" -> "Bắt đầu học"
                        isEnrolled -> "Tiếp tục học"
                        else -> "Mua ngay – $price"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

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