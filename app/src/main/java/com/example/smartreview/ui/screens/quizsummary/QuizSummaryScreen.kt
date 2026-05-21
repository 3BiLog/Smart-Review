package com.example.smartreview.ui.screens.quizsummary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.theme.*

@Composable
fun QuizSummaryScreen(
    navController: NavHostController,
    sessionId: String,
    vm: QuizSummaryViewModel = viewModel(factory = QuizSummaryViewModel.provideFactory(sessionId)),
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
                if (state.passed) Icons.Default.CheckCircle else Icons.Default.Star,
                contentDescription = null,
                tint = if (state.passed) Secondary else Tertiary,
                modifier = Modifier.size(72.dp),
            )
            Text(
                if (state.passed) "Đạt quiz!" else "Hoàn thành quiz",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
            Text(state.quizTitle, style = MaterialTheme.typography.titleMedium, color = OnSurface)

            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryRow("Điểm số", "${(state.scorePercent * 100).toInt()}%")
                    SummaryRow("Câu đúng", "${state.correctCount}/${state.totalQuestions}")
                    SummaryRow("Thời gian", state.studyTime)
                    SummaryRow("Ngưỡng đạt", if (state.passed) "Đạt" else "Chưa đạt")
                    if (state.rewardGranted) {
                        SummaryRow("XP", "+${state.xpEarned}")
                        SummaryRow("Streak", "${state.streakDays} ngày")
                    } else if (state.hasSessionData) {
                        Text(
                            "XP quiz đã được nhận trước đó (mỗi quiz một lần).",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                        )
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Icon(Icons.Default.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("Quay lại bài học")
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
        Text(label, color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = OnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
