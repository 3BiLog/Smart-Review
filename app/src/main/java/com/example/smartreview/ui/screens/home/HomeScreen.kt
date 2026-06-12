package com.example.smartreview.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.data.model.LearningActivityType
import com.example.smartreview.ui.components.SectionHeader
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.navigation.Screen
import com.example.smartreview.ui.screens.home.components.*
import com.example.smartreview.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    vm: HomeViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshResumeLearning()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = { SmartReviewBottomBar(navController) },
        topBar = {
            HomeTopBar(
                userName = state.userName,
                xp = state.xp.toInt(),      // FIXED: Convert Long to Int
                streak = state.streak.toInt(),  // FIXED: Convert Long to Int
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            HomeDailyGoalCard(
                progress = state.goalProgress,
                current = state.goalCurrent,
                target = state.goalTarget,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(24.dp))

            SectionHeader(
                title = "Tiếp tục học",
                linkText = if (state.resumeLearning.isNotEmpty()) "Xem tất cả" else null,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
            if (state.resumeLearning.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.resumeLearning, key = { "${it.type}_${it.contentId}" }) { item ->
                        HomeContinueCourseCard(
                            card = CourseCard(
                                id = item.courseId ?: item.contentId,
                                title = item.title,
                                subtitle = item.subtitle,
                                imageUrl = item.imageUrl,
                                progress = item.progressPercent,
                                timeLeft = when (item.type) {
                                    LearningActivityType.LESSON -> "Bài học dở"
                                    LearningActivityType.QUIZ -> "Quiz dở"
                                    LearningActivityType.FLASHCARD -> "Flashcard dở"
                                },
                            ),
                            onClick = { navController.navigate(item.route) },
                        )
                    }
                }
            } else {
                Text(
                    "Chưa có bài học đang dở. Bắt đầu một khóa học để thấy tiến độ tại đây.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            SectionHeader(
                title = "Recommended",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
            state.recommended.forEach { item ->
                HomeRecommendedItem(
                    item = item,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Pomodoro.route) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Primary.copy(0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Open Pomodoro Timer", color = Primary)
                }
            }
        }
    }
}