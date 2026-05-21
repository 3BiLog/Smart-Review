package com.example.smartreview.ui.screens.lessonsummary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.theme.*

@Composable
fun LessonSummaryScreen(
    navController: NavHostController,
    sessionId: String,
    vm: LessonSummaryViewModel = viewModel(factory = LessonSummaryViewModel.provideFactory(sessionId)),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = Background) { padding ->
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
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(72.dp),
            )
            Text(
                "Hoàn thành bài học!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
            Text(
                state.lessonTitle,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
            )

            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryRow("Block đã học", "${state.viewedBlocks}/${state.totalBlocks}")
                    SummaryRow("Thời gian", state.studyTime)
                    SummaryRow("Tiến độ", "${(state.progress * 100).toInt()}%")
                    if (state.rewardGranted) {
                        SummaryRow("XP nhận được", "+${state.xpEarned}")
                        SummaryRow("Streak", "${state.streakDays} ngày")
                    } else {
                        Text(
                            "XP đã được nhận trước đó cho bài học này.",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                        )
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Icon(Icons.Default.ArrowForward, null)
                Spacer(Modifier.width(8.dp))
                Text("Quay lại khóa học")
            }

            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Star, null, tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text("Về trang chủ", color = Primary)
            }
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
