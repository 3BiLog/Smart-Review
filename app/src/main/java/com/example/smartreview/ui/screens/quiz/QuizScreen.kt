package com.example.smartreview.ui.screens.quiz

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.smartreview.data.learning.StudyTimeManager
import com.example.smartreview.ui.screens.quizsummary.quizSummaryRoute
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    navController: NavHostController,
    quizId: String,
    vm: QuizViewModel = viewModel(factory = QuizViewModel.provideFactory(quizId)),
) {
    android.util.Log.d("QuizScreen", ">>> QuizScreen launched with quizId=$quizId")
    val state by vm.uiState.collectAsStateWithLifecycle()
    val quiz = state.quiz
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

    android.util.Log.d("QuizScreen", "UI rendering, isLoading=${state.isLoading}, quiz=${quiz?.title}, questions=${quiz?.questions?.size}")

    DisposableEffect(Unit) {
        StudyTimeManager.startTracking("QuizScreen", onGoalCompleted)
        onDispose {
            StudyTimeManager.stopTracking()
        }
    }



    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    if (quiz == null || vm.currentQuestion == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy quiz.", color = OnSurfaceVariant)
        }
        return
    }

    val question = vm.currentQuestion!!
    val progress = (state.currentIndex + 1).toFloat() / vm.totalQuestions.coerceAtLeast(1)
    val isLastQuestion = state.currentIndex >= vm.totalQuestions - 1
    val showFinishedPanel = state.isQuizFinished ||
            (state.showFeedback && isLastQuestion && state.answers.size >= vm.totalQuestions)

    Scaffold(
        containerColor = Background,
        topBar = {
            QuizTopBar(
                title = quiz.title,
                subtitle = "Câu ${state.currentIndex + 1}/${vm.totalQuestions}",
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
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Tiếp tục quiz từ lần trước",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            if (quiz.description.isNotBlank()) {
                Text(quiz.description, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(16.dp))
            }

            if (showFinishedPanel) {
                QuizFinishedPanel(
                    correct = state.answers.count { it.isCorrect },
                    total = vm.totalQuestions,
                    onFinish = {
                        val result = vm.completeQuiz() ?: return@QuizFinishedPanel
                        navController.navigate(quizSummaryRoute(result.sessionId)) {
                            launchSingleTop = true
                            popUpTo("quiz/$quizId") { inclusive = true }
                        }
                    },
                )
            } else {
                Text(
                    question.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                )
                Spacer(Modifier.height(16.dp))

                question.options.forEachIndexed { index, optionLabel ->
                    val selectedOptionId = state.selectedOptionId
                    val isSelected = selectedOptionId?.toIntOrNull() == index
                    val showResult = state.showFeedback
                    val isCorrectOption = index == question.correctOptionIndex
                    val containerColor = when {
                        !showResult && isSelected -> Primary.copy(alpha = 0.15f)
                        showResult && isCorrectOption -> Secondary.copy(alpha = 0.2f)
                        showResult && isSelected && !isCorrectOption -> ErrorColor.copy(alpha = 0.15f)
                        else -> SurfaceContainer
                    }
                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) Primary else GlassBorder,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable(enabled = !state.showFeedback) {
                                vm.selectOption(index.toString())
                            },
                    ) {
                        Text(
                            optionLabel,
                            modifier = Modifier.padding(16.dp),
                            color = OnSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                if (state.showFeedback) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = if (state.lastFeedbackCorrect) Secondary.copy(0.12f) else ErrorColor.copy(0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                if (state.lastFeedbackCorrect) "Chính xác!" else "Chưa đúng",
                                fontWeight = FontWeight.Bold,
                                color = if (state.lastFeedbackCorrect) Secondary else ErrorColor,
                            )
                            if (state.lastExplanation.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(state.lastExplanation, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = { vm.previousQuestion() },
                        enabled = state.currentIndex > 0 && !state.showFeedback,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Trước")
                    }
                    if (!state.showFeedback) {
                        Button(
                            onClick = { vm.submitAnswer() },
                            enabled = state.selectedOptionId != null,
                            modifier = Modifier.weight(1.2f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        ) {
                            Text("Kiểm tra")
                        }
                    } else {
                        Button(
                            onClick = { vm.nextQuestion() },
                            modifier = Modifier.weight(1.2f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        ) {
                            Text(
                                if (state.currentIndex >= vm.totalQuestions - 1) "Kết quả" else "Tiếp theo",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizFinishedPanel(
    correct: Int,
    total: Int,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.EmojiEvents, null, tint = Primary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Bạn đã hoàn thành quiz!", style = MaterialTheme.typography.titleLarge, color = Primary)
        Text(
            "$correct/$total câu đúng",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Xem kết quả & nhận XP")
        }
    }
}

@Composable
private fun QuizTopBar(title: String, subtitle: String, onClose: () -> Unit) {
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
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Spacer(Modifier.width(48.dp))
        }
    }
}