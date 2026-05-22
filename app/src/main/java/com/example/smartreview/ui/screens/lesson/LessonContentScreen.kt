package com.example.smartreview.ui.screens.lesson

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.model.LessonBlock
import com.example.smartreview.data.model.LessonBlockType
import com.example.smartreview.ui.screens.lessonsummary.lessonSummaryRoute
import com.example.smartreview.ui.screens.quiz.quizRoute
import com.example.smartreview.ui.theme.*

/**
 * Theory / summary step after [com.example.smartreview.ui.screens.lessonplayer.LessonVideoPlayerScreen].
 */
@Composable
fun LessonContentScreen(
    navController: NavHostController,
    lessonId: String,
    vm: LessonViewModel = viewModel(factory = LessonViewModel.provideFactory(lessonId)),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val lesson = state.lesson

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    if (lesson == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy bài học.", color = OnSurfaceVariant)
        }
        return
    }

    LaunchedEffect(lesson.id) {
        vm.markContentViewed()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            LessonContentTopBar(
                title = lesson.title,
                subtitle = "Tóm tắt & điểm chính",
                onClose = { navController.popBackStack() },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            if (state.isResuming) {
                Surface(
                    color = Primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Text(
                        "Tiếp tục nội dung bài học",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            if (state.alreadyCompleted) {
                Surface(
                    color = Secondary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                ) {
                    Text(
                        "Bạn đã hoàn thành bài học này. XP chỉ nhận một lần mỗi bài.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    lesson.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                state.contentBlocks.forEach { block ->
                    LessonContentBlockView(block = block)
                    Spacer(Modifier.height(16.dp))
                }
                if (state.contentBlocks.isEmpty()) {
                    Text(
                        "Bài học không có nội dung tóm tắt bổ sung.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = {
                    val quizId = state.linkedQuizId
                    if (!quizId.isNullOrBlank()) {
                        navController.navigate(quizRoute(quizId)) {
                            launchSingleTop = true
                            popUpTo("lesson/$lessonId") { inclusive = false }
                        }
                    } else {
                        val sessionId = vm.completeLesson()
                        if (sessionId != null) {
                            navController.navigate(lessonSummaryRoute(sessionId)) {
                                launchSingleTop = true
                                popUpTo("lesson/$lessonId") { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Icon(
                    if (state.linkedQuizId != null) Icons.Default.Quiz else Icons.Default.EmojiEvents,
                    contentDescription = null,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.linkedQuizId != null) "Tiếp tục — Làm quiz" else "Hoàn thành bài học",
                )
            }
        }
    }
}

@Composable
private fun LessonContentBlockView(block: LessonBlock) {
    when (block.type) {
        LessonBlockType.HEADING -> {
            Text(
                block.title.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
        }
        LessonBlockType.TEXT -> {
            Text(
                block.body,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
        }
        LessonBlockType.IMAGE -> {
            block.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                )
            }
        }
        LessonBlockType.TIP -> {
            Surface(
                color = Tertiary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        block.title ?: "Điểm chính",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Tertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(block.body, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
            }
        }
        LessonBlockType.QUIZ_STUB -> Unit
    }
}

@Composable
private fun LessonContentTopBar(
    title: String,
    subtitle: String,
    onClose: () -> Unit,
) {
    Surface(color = GlassBg) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null, tint = OnSurfaceVariant)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    maxLines = 1,
                )
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Spacer(Modifier.width(48.dp))
        }
    }
}
