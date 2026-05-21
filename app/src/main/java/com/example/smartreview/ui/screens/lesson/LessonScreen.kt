package com.example.smartreview.ui.screens.lesson

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun LessonScreen(
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

    if (lesson == null || vm.currentBlock == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy bài học.", color = OnSurfaceVariant)
        }
        return
    }

    val block = vm.currentBlock!!
    val progress = state.viewedBlockCount.toFloat() / vm.totalBlocks.coerceAtLeast(1)

    LaunchedEffect(state.currentBlockIndex) {
        vm.markCurrentBlockViewed()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            LessonLearningTopBar(
                title = lesson.title,
                subtitle = "${state.currentBlockIndex + 1}/${vm.totalBlocks} · còn ${state.remainingBlockCount}",
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
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Primary,
                trackColor = SurfaceVariant,
            )

            if (state.isResuming) {
                Surface(
                    color = Primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                ) {
                    Text(
                        "Tiếp tục bài học từ lần trước",
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
                        "Bạn đã hoàn thành bài học này trước đó. XP chỉ nhận một lần mỗi bài.",
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
                LessonBlockContent(
                    block = block,
                    onStartQuiz = { quizId ->
                        navController.navigate(quizRoute(quizId)) { launchSingleTop = true }
                    },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { vm.previousBlock() },
                    enabled = state.currentBlockIndex > 0 && !state.isLessonComplete,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.ChevronLeft, null, modifier = Modifier.size(18.dp))
                    Text("Trước")
                }

                if (state.isLessonComplete) {
                    Button(
                        onClick = {
                            val sessionId = vm.completeLesson()
                            if (sessionId != null) {
                                navController.navigate(lessonSummaryRoute(sessionId)) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Icon(Icons.Default.EmojiEvents, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Hoàn thành")
                    }
                } else {
                    Button(
                        onClick = { vm.nextBlock() },
                        enabled = state.currentBlockIndex < vm.totalBlocks - 1,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("Tiếp theo")
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonBlockContent(
    block: LessonBlock,
    onStartQuiz: (String) -> Unit,
) {
    when (block.type) {
        LessonBlockType.HEADING -> {
            Text(
                block.title.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
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
                        block.title ?: "Mẹo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Tertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(block.body, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
            }
        }
        LessonBlockType.QUIZ_STUB -> {
            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Quiz, null, tint = Primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                block.body,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurface,
                            )
                            Text(
                                "Kiểm tra kiến thức sau bài học",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                            )
                        }
                    }
                    block.quizStubId?.let { quizId ->
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onStartQuiz(quizId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        ) {
                            Text("Bắt đầu quiz")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonLearningTopBar(
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
