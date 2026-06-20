package com.example.smartreview.ui.screens.quizsummary

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateLessonVideo
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateLessonContent
import com.example.smartreview.ui.navigation.Screen
import com.example.smartreview.ui.screens.quiz.quizRoute
import com.example.smartreview.ui.theme.*

@Composable
fun QuizSummaryScreen(
    navController: NavHostController,
    sessionId: String,
    vm: QuizSummaryViewModel = viewModel(factory = QuizSummaryViewModel.provideFactory(sessionId)),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        topBar = {
            Surface(color = GlassBg, tonalElevation = 0.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Text(
                        "KẾT QUẢ QUIZ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                if (state.passed) Icons.Default.CheckCircle else Icons.Default.Star,
                contentDescription = null,
                tint = if (state.passed) Secondary else Tertiary,
                modifier = Modifier.size(72.dp),
            )
            Text(
                if (state.passed) "Chúc mừng bạn!" else "Hoàn thành quiz!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (state.passed) Secondary else Primary,
            )
            Text(
                state.quizTitle,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryRow("Điểm số", "${state.scorePercent.toInt()}%")
                    SummaryRow("Câu đúng", "${state.correctCount}/${state.totalQuestions}")
                    SummaryRow("Thời gian", state.studyTime)
                    SummaryRow("Trạng thái", if (state.passed) "Đạt" else "Chưa đạt")
                    if (state.rewardGranted) {
                        SummaryRow("XP nhận được", "+${state.xpEarned}")
                        SummaryRow("Streak", "${state.streakDays} ngày")
                    } else if (state.hasSessionData) {
                        state.rewardMessage?.let { message ->
                            Text(
                                message,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isLastLessonOfLastModule = state.isLastLessonInModule && state.isLastModule
            val hasNextLesson = state.hasNextLesson && state.nextLessonId != null

            when {
                isLastLessonOfLastModule -> {
                    Button(
                        onClick = {
                            navController.navigate("course_detail/${state.courseId}") {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Hoàn thành khóa học")
                    }
                }

                hasNextLesson -> {
                    Button(
                        onClick = {
                            val nextLessonId = state.nextLessonId
                            val nextLessonType = state.nextLessonType
                            if (nextLessonId != null) {
                                navigateToNextLesson(
                                    navController,
                                    nextLessonId,
                                    nextLessonType,
                                    state.courseId,
                                    state.nextLessonQuizId,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Icon(Icons.Default.ArrowForward, null)
                        Spacer(Modifier.width(8.dp))
                        val lessonTypeLabel = when (state.nextLessonType) {
                            LessonType.VIDEO -> "🎬"
                            LessonType.READING -> "📖"
                            LessonType.QUIZ -> "📝"
                            LessonType.FLASHCARD -> "🃏"
                            else -> "▶️"
                        }
                        val title = state.nextLessonTitle?.take(30) ?: "Bài học tiếp theo"
                        Text("$title")
                    }
                }

                else -> {
                    Button(
                        onClick = {
                            navController.navigate("course_detail/${state.courseId}") {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Quay lại khóa học")
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Home, null, tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text("Về trang chủ", color = Primary)
            }
        }
    }
}

private fun navigateToNextLesson(
    navController: NavHostController,
    nextLessonId: String,
    lessonType: LessonType?,
    courseId: String,
    nextLessonQuizId: String? = null,
) {
    when (lessonType) {
        LessonType.VIDEO, LessonType.UNKNOWN -> {
            navController.navigateLessonVideo(nextLessonId, courseId = courseId)
        }
        LessonType.READING -> {
            navController.navigateLessonContent(nextLessonId)
        }
        LessonType.QUIZ -> {
            navController.navigate(quizRoute(nextLessonQuizId ?: nextLessonId)) {
                launchSingleTop = false
                restoreState = false
            }
        }
        LessonType.FLASHCARD -> {
            navController.navigate("flashcard/${nextLessonId}") {
                launchSingleTop = false
                restoreState = false
            }
        }
        else -> {
            navController.navigateLessonVideo(nextLessonId, courseId = courseId)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = OnSurface)
    }
}