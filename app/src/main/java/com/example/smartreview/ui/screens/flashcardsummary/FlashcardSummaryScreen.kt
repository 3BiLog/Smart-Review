package com.example.smartreview.ui.screens.flashcardsummary

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateLessonContent
import com.example.smartreview.ui.navigation.LearningFlowNavigation.navigateLessonVideo
import com.example.smartreview.ui.navigation.Screen
import com.example.smartreview.ui.screens.flashcardsummary.components.*
import com.example.smartreview.ui.screens.quiz.quizRoute
import com.example.smartreview.ui.theme.Background
import com.example.smartreview.ui.theme.Primary
import com.example.smartreview.ui.theme.SmartReviewTheme
import com.example.smartreview.ui.theme.Tertiary

@Composable
fun FlashcardSummaryScreen(
    navController: NavHostController,
    sessionId: String,
    vm: FlashcardSummaryViewModel = viewModel(factory = FlashcardSummaryViewModel.provideFactory(sessionId)),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    val ringProgress by animateFloatAsState(
        targetValue = state.animatedAccuracy,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "accuracyRing",
    )

    Scaffold(
        containerColor = Background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            FlashcardSummaryAmbientGlow()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 40.dp),
            ) {
                FlashcardSummaryTrophyHero(
                    xpEarned = if (state.rewardGranted) state.xpEarned else 0,
                )
                state.rewardMessage?.let { message ->
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.Text(
                        text = message,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = com.example.smartreview.ui.theme.OnSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(32.dp))

                FlashcardSummaryAccuracyTile(
                    progress = ringProgress,
                    accuracy = state.accuracy,
                    knownCount = state.knownCount,
                    reviewCount = state.reviewCount,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FlashcardSummaryStatTile(
                        icon = Icons.Default.LocalFireDepartment,
                        tint = Tertiary,
                        label = "Chuỗi ngày",
                        value = state.streakDays.toString(),
                        sub = "Ngày liên tiếp",
                        modifier = Modifier.weight(1f),
                    )
                    FlashcardSummaryStatTile(
                        icon = Icons.Default.Timer,
                        tint = Primary,
                        label = "Thời gian",
                        value = state.studyTime,
                        sub = "Phút học tập",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(40.dp))

                val isLastLessonOfLastModule = state.isLastLessonInModule && state.isLastModule
                val hasNextLesson = state.hasNextLesson && state.nextLessonId != null

                val (primaryText, primaryIcon) = when {
                    isLastLessonOfLastModule -> "Hoàn thành khóa học" to Icons.Default.CheckCircle
                    hasNextLesson -> {
                        val title = state.nextLessonTitle?.take(30) ?: "Bài học tiếp theo"
                        "Bài học tiếp theo: $title" to Icons.Default.ArrowForward
                    }
                    else -> "Quay lại khóa học" to Icons.Default.ArrowBack
                }

                FlashcardSummaryGradientButton(
                    text = primaryText,
                    icon = primaryIcon,
                    loading = state.isNavigating,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        vm.onPrimaryAction {
                            when {
                                hasNextLesson -> {
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
                                }
                                else -> {
                                    navController.navigate("course_detail/${state.courseId}") {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    },
                )

                Spacer(Modifier.height(12.dp))

                FlashcardSummaryReviewButton(
                    text = "Về trang chủ",
                    icon = Icons.Default.Home,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
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

@Preview(
    name = "FlashcardSummary – Dark",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun FlashcardSummaryPreview() {
    SmartReviewTheme {
        FlashcardSummaryScreen(
            navController = rememberNavController(),
            sessionId = "preview",
        )
    }
}
