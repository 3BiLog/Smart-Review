package com.example.smartreview.ui.screens.lessonplayer

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.learning.StudyTimeManager
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.ui.components.YoutubeLessonPlayer
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateLessonContent
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateLessonVideo
import com.example.smartreview.ui.navigation.RouteHelpers
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.delay

const val LESSON_PLAYER_ROUTE = "lesson_player/{lessonId}"
fun lessonPlayerRoute(lessonId: String) = "lesson_player/$lessonId"

@Composable
fun LessonVideoPlayerScreen(
    navController: NavHostController,
    lessonId: String,
    courseId: String? = null,
    vm: LessonPlayerViewModel = viewModel(
        factory = LessonPlayerViewModel.provideFactory(lessonId, courseId)
    ),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val lesson = state.currentLesson ?: return
    var showGoalCompleted by remember { mutableStateOf(false) }
    var xpEarned by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    val onGoalCompleted: (Long) -> Unit = remember {
        { xp ->
            xpEarned = xp
            showGoalCompleted = true
        }
    }

    if (showGoalCompleted) {
        LaunchedEffect(showGoalCompleted) {
            Toast.makeText(
                context,
                "Hoàn thành mục tiêu hôm nay! +$xpEarned XP",
                Toast.LENGTH_LONG
            ).show()
            delay(3000)
            showGoalCompleted = false
        }
    }

    DisposableEffect(lessonId) {
        android.util.Log.d("LessonVideoPlayerScreen", "Screen CREATED for lesson: $lessonId")
        onDispose {
            android.util.Log.d("LessonVideoPlayerScreen", "Screen DESTROYED for lesson: $lessonId")
        }
    }

    DisposableEffect(Unit) {
        StudyTimeManager.startTracking("LessonVideoPlayerScreen", onGoalCompleted)
        onDispose {
            StudyTimeManager.stopTracking()
        }
    }


    if (state.showCompleteDialog) {
        val isFromUpNext = state.selectedNextLesson != null
        val targetLesson = state.selectedNextLesson ?: state.upNextLessons.firstOrNull()

        AlertDialog(
            onDismissRequest = {
                vm.dismissCompleteDialog()
                vm.clearSelectedNextLesson()
            },
            title = {
                Text(
                    "Hoàn thành bài học?",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    Text("Bạn đã xem xong bài học này chưa?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Hoàn thành bài học sẽ giúp bạn nhận được XP và theo dõi tiến độ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    if (isFromUpNext && targetLesson != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sau đó sẽ chuyển đến: ${targetLesson.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isFromUpNext && targetLesson != null) {
                            vm.completeLessonAndNavigateToSelected { selectedLesson ->
                                if (selectedLesson != null) {
                                    navigateToLesson(navController, selectedLesson, courseId, lesson.id)
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            vm.completeLessonAndContinue { nextLessonId ->
                                if (nextLessonId != null) {
                                    val targetRoute = if (courseId.isNullOrBlank()) {
                                        lessonPlayerRoute(nextLessonId)
                                    } else {
                                        RouteHelpers.lessonPlayerRoute(courseId, nextLessonId)
                                    }
                                    navController.navigate(targetRoute) {
                                        val currentRoute = if (courseId.isNullOrBlank()) {
                                            lessonPlayerRoute(lesson.id)
                                        } else {
                                            RouteHelpers.lessonPlayerRoute(courseId, lesson.id)
                                        }
                                        popUpTo(currentRoute) { inclusive = true }
                                        launchSingleTop = false
                                        restoreState = false
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        }
                    },
                    enabled = !state.isCompleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (state.isCompleting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Đã hoàn thành")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.dismissCompleteDialog()
                    vm.clearSelectedNextLesson()
                }) {
                    Text("Chưa xong")
                }
            }
        )
    }

    key(lessonId) {
        Scaffold(
            containerColor = Background,
            topBar = {
                LessonPlayerTopBar(
                    onBack = { navController.popBackStack() },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    val hasBlocks = state.hasContentBlocks
                    val nextLesson = state.upNextLessons.firstOrNull()
                    val ctaText = if (hasBlocks) "Tiếp tục — Tóm tắt bài học" else (if (nextLesson != null) "Bài học tiếp theo" else "Hoàn thành khóa học")

                    when (lesson.lessonType) {
                        LessonType.VIDEO, LessonType.UNKNOWN -> {
                            VideoPlayerArea(
                                videoId = state.youtubeVideoId,
                                thumbnailUrl = lesson.thumbnailUrl,
                                videoError = state.videoError,
                                ctaText = ctaText,
                                onContinue = {
                                    if (hasBlocks) {
                                        navController.navigateLessonContent(lesson.id)
                                    } else {
                                        val nextLessonCandidate = state.upNextLessons.firstOrNull()
                                        if (nextLessonCandidate != null) {
                                            vm.setSelectedNextLesson(nextLessonCandidate)
                                        } else {
                                            vm.clearSelectedNextLesson()
                                        }
                                        vm.showCompleteConfirmation()
                                    }
                                },
                            )
                        }
                        LessonType.QUIZ -> {
                            QuizLessonPreview(
                                lesson = lesson,
                                onStartQuiz = {
                                    val quizIdToUse = lesson.quizId?.takeIf { it.isNotBlank() } ?: lesson.id
                                    navController.navigate(com.example.smartreview.ui.screens.quiz.quizRoute(quizIdToUse)) {
                                        launchSingleTop = false
                                        restoreState = false
                                    }
                                },
                                onComplete = {
                                    vm.showCompleteConfirmation()
                                    vm.clearSelectedNextLesson()
                                },
                                ctaText = ctaText
                            )
                        }
                        LessonType.READING -> {
                            ReadingLessonPreview(
                                lesson = lesson,
                                onStartReading = {
                                    navController.navigateLessonContent(lesson.id)
                                },
                                onComplete = {
                                    vm.showCompleteConfirmation()
                                    vm.clearSelectedNextLesson()
                                },
                                ctaText = ctaText
                            )
                        }
                        LessonType.FLASHCARD -> {
                            FlashcardLessonPreview(
                                lesson = lesson,
                                onStartFlashcard = {
                                    navController.navigate("flashcard/${lesson.id}") {
                                        launchSingleTop = false
                                        restoreState = false
                                    }
                                },
                                onComplete = {
                                    vm.showCompleteConfirmation()
                                    vm.clearSelectedNextLesson()
                                },
                                ctaText = ctaText
                            )
                        }
                    }
                }

                item {
                    LessonInfoCard(
                        lesson = lesson,
                        subtitle = state.lessonSubtitle,
                        isSaved = state.isSaved,
                        onSave = { vm.toggleSave() },
                        modifier = Modifier.padding(16.dp),
                    )
                }

                item {
                    LessonTagsRow(
                        tags = listOf("#Async", "#Performance", "#Advanced"),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item {
                    Text(
                        "Up Next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                items(state.upNextLessons, key = { it.id }) { playlistItem ->
                    PlaylistItem(
                        item = playlistItem,
                        isCurrent = playlistItem.isCurrentlyPlaying,
                        onClick = {
                            if (playlistItem.id == lesson.id) return@PlaylistItem
                            vm.setSelectedNextLesson(playlistItem)
                            vm.showCompleteConfirmation()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private fun navigateToLesson(
    navController: NavHostController,
    lesson: LessonItem,
    courseId: String?,
    currentLessonId: String
) {
    when (lesson.lessonType) {
        LessonType.VIDEO, LessonType.UNKNOWN -> {
            val targetRoute = if (courseId.isNullOrBlank()) {
                lessonPlayerRoute(lesson.id)
            } else {
                RouteHelpers.lessonPlayerRoute(courseId, lesson.id)
            }
            navController.navigate(targetRoute) {
                val currentRoute = if (courseId.isNullOrBlank()) {
                    lessonPlayerRoute(currentLessonId)
                } else {
                    RouteHelpers.lessonPlayerRoute(courseId, currentLessonId)
                }
                popUpTo(currentRoute) { inclusive = true }
                launchSingleTop = false
                restoreState = false
            }
        }
        LessonType.READING -> {
            navController.navigateLessonContent(lesson.id)
        }
        LessonType.QUIZ -> {
            val quizIdToUse = lesson.quizId?.takeIf { it.isNotBlank() } ?: lesson.id
            navController.navigate(com.example.smartreview.ui.screens.quiz.quizRoute(quizIdToUse)) {
                launchSingleTop = false
                restoreState = false
            }
        }
        LessonType.FLASHCARD -> {
            navController.navigate("flashcard/${lesson.id}") {
                launchSingleTop = false
                restoreState = false
            }
        }
    }
}

@Composable
private fun VideoPlayerArea(
    videoId: String?,
    thumbnailUrl: String,
    videoError: String?,
    ctaText: String = "Tiếp tục — Tóm tắt bài học",
    onContinue: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
        ) {
            if (videoId != null) {
                androidx.compose.runtime.key("player_$videoId") {
                    YoutubeLessonPlayer(
                        videoId = videoId,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                AsyncImage(
                    model = thumbnailUrl.ifEmpty { "https://picsum.photos/seed/video/640/360" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.45f)),
                ) {
                    Text(
                        videoError ?: "Chưa có video",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(ctaText)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUIZ LESSON PREVIEW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuizLessonPreview(
    lesson: LessonItem,
    onStartQuiz: () -> Unit,
    onComplete: () -> Unit,
    ctaText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Quiz,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Bài học dạng Quiz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Hãy chuẩn bị tinh thần để làm bài kiểm tra!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bắt đầu làm quiz")
                }
            }
        }
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(ctaText)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// READING LESSON PREVIEW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ReadingLessonPreview(
    lesson: LessonItem,
    onStartReading: () -> Unit,
    onComplete: () -> Unit,
    ctaText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1B4332),
                            Color(0xFF2D6A4F),
                            Color(0xFF40916C)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Secondary.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Bài học dạng Reading",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Đọc và tìm hiểu nội dung bài học",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartReading,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Description, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bắt đầu đọc")
                }
            }
        }
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(ctaText)
        }
    }
}

@Composable
private fun FlashcardLessonPreview(
    lesson: LessonItem,
    onStartFlashcard: () -> Unit,
    onComplete: () -> Unit,
    ctaText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4A1942),
                            Color(0xFF6B2D5C),
                            Color(0xFF8B4A7A)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Tertiary.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Style,
                        contentDescription = null,
                        tint = Tertiary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Bài học dạng Flashcard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Học từ vựng với thẻ ghi nhớ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartFlashcard,
                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Style, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bắt đầu học flashcard")
                }
            }
        }
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(ctaText)
        }
    }
}

@Composable
private fun LessonInfoCard(
    lesson: LessonItem,
    subtitle: String,
    isSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = SurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            "Module 3",
                            color = Secondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                    )
                    if (subtitle.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                        )
                    }
                }
                IconButton(
                    onClick = onSave,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainer),
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                        contentDescription = "Save",
                        tint = if (isSaved) Primary else OnSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = GlassBorder)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem(icon = Icons.Default.Visibility, text = "1.2k views")
                StatItem(icon = Icons.Default.AccessTime, text = lesson.formattedDuration)
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
    }
}

@Composable
private fun LessonTagsRow(tags: List<String>, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        tags.forEach { tag ->
            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(50.dp)),
            ) {
                Text(
                    text = tag,
                    color = Primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    item: LessonItem,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (isCurrent) SurfaceVariant else Background,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.border(1.dp, Primary.copy(0.4f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .then(
                if (item.isLocked) Modifier else Modifier.clickable(onClick = onClick)
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .then(if (item.isLocked) Modifier.alpha(0.55f) else Modifier),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(112.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant),
            ) {
                if (!item.isLocked && item.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(4.dp)
                            .align(Alignment.CenterStart)
                            .background(Brush.linearGradient(listOf(GradientStart, Secondary))),
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(if (isCurrent) 0.35f else 0.20f)),
                ) {
                    Icon(
                        imageVector = when {
                            item.isLocked -> Icons.Default.Lock
                            isCurrent -> Icons.Default.PlayCircle
                            else -> Icons.Default.PlayCircle
                        },
                        contentDescription = null,
                        tint = when {
                            item.isLocked -> OnSurfaceVariant
                            isCurrent -> Primary
                            else -> Color.White.copy(0.8f)
                        },
                        modifier = Modifier.size(24.dp),
                    )
                }

                if (!item.isLocked && item.durationSeconds > 0) {
                    Surface(
                        color = Color.Black.copy(0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                    ) {
                        Text(
                            item.formattedDuration,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) Primary else OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when {
                        isCurrent -> "Now Playing"
                        item.isLocked -> "Complete previous lessons"
                        else -> "Lesson"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) Secondary else OnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LessonPlayerTopBar(onBack: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text(
                "SMART REVIEW",
                style = MaterialTheme.typography.titleMedium.copy(
                    brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, null, tint = Primary)
            }
        }
    }
}

private fun Modifier.alpha(alpha: Float) = this.then(
    Modifier.graphicsLayer { this.alpha = alpha }
)