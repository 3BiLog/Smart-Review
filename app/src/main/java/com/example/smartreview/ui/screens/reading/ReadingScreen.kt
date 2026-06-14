package com.example.smartreview.ui.screens.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.delay

const val READING_ROUTE = "reading/{lessonId}"
fun readingRoute(lessonId: String) = "reading/$lessonId"

@OptIn(ExperimentalMaterial3Api::class)  // FIXED: Add this annotation
@Composable
fun ReadingScreen(
    navController: NavHostController,
    lessonId: String,
    viewModel: ReadingViewModel = viewModel(factory = ReadingViewModel.provideFactory(lessonId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    android.util.Log.d("ReadingScreen", "ReadingScreen launched with lessonId=$lessonId, isLoading=${state.isLoading}")

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    if (state.reading == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Không tìm thấy bài đọc.", color = OnSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Quay lại")
                }
            }
        }
        return
    }

    val reading = state.reading!!

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        reading.title,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassBg),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = SurfaceContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (reading.markdown.isNotBlank()) {
                        Text(
                            text = reading.markdown,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface,
                            lineHeight = 24.sp
                        )
                    } else {
                        Text(
                            text = "Nội dung đang được cập nhật...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.completeReading() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !state.isCompleted && !state.isCompleting
            ) {
                if (state.isCompleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else if (state.isCompleted) {
                    Text("✅ Đã hoàn thành")
                } else {
                    Text("📖 Đánh dấu đã đọc (+${reading.xpReward} XP)")
                }
            }

            if (state.showSuccess) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    viewModel.dismissSuccess()
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = {
                            viewModel.dismissSuccess()
                            navController.popBackStack()
                        }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text("✅ Hoàn thành! +${reading.xpReward} XP")
                }
            }

            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = ErrorColor,
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Đóng", color = Color.White)
                        }
                    }
                ) {
                    Text(error, color = Color.White)
                }
            }
        }
    }
}