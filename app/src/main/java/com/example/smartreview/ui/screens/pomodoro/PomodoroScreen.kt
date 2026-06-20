package com.example.smartreview.ui.screens.pomodoro

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.theme.*

@Composable
fun PomodoroScreen(
    navController: NavHostController,
    vm: PomodoroViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        bottomBar      = { SmartReviewBottomBar(navController) },
        topBar         = { PomodoroTopBar() }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Spacer(Modifier.height(24.dp))

            RingTimer(
                progress    = state.progress,
                displayTime = state.displayTime,
                label       = if (state.status == TimerStatus.BREAK) "Break Session" else "Focus Session",
                modifier    = Modifier.size(280.dp)
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { idx ->
                    val active = idx < state.cycle
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (active) Secondary else SurfaceVariant)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ControlButton(onClick = { vm.reset() }, size = 56) {
                    Icon(Icons.Default.Replay, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                        .clickable { vm.togglePlayPause() }
                ) {
                    Icon(
                        if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                ControlButton(onClick = { vm.skipToNext() }, size = 56) {
                    Icon(Icons.Default.SkipNext, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            Surface(
                color    = SurfaceContainer,
                shape    = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Timer Settings", style = MaterialTheme.typography.titleSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                    }

                    SettingRow(
                        label   = "Work Time",
                        value   = "${state.workMinutes}",
                        onMinus = { vm.decrementWork() },
                        onPlus  = { vm.incrementWork() }
                    )
                    SettingRow(
                        label   = "Break Time",
                        value   = "${state.breakMinutes}",
                        onMinus = { vm.decrementBreak() },
                        onPlus  = { vm.incrementBreak() }
                    )

                    HorizontalDivider(color = GlassBorder)

                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Chế độ tập trung sâu", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
                            Text("Blocks notifications during work", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked         = state.deepFocusEnabled,
                            onCheckedChange = { vm.toggleDeepFocus() },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = Primary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RingTimer(progress: Float, displayTime: String, label: String, modifier: Modifier = Modifier) {
    val animProg by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(800, easing = LinearEasing),
        label         = "progress"
    )
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val r      = (size.minDimension - stroke) / 2f
            val tl     = Offset((size.width - r * 2) / 2f, (size.height - r * 2) / 2f)
            val sz     = Size(r * 2, r * 2)
            // Track ring
            drawArc(color = SurfaceVariant, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round), topLeft = tl, size = sz)
            // Progress ring
            drawArc(
                brush     = Brush.linearGradient(listOf(GradientStart, Secondary), start = Offset.Zero, end = Offset(size.width, size.height)),
                startAngle = -90f, sweepAngle = 360f * animProg, useCenter = false,
                style      = Stroke(stroke, cap = StrokeCap.Round), topLeft = tl, size = sz
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text     = displayTime,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color    = OnSurface,
                letterSpacing = 2.sp
            )
            Text(label, style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun ControlButton(onClick: () -> Unit, size: Int, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .border(1.dp, GlassBorder, CircleShape)
            .clickable(onClick = onClick)
    ) { content() }
}

@Composable
private fun SettingRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Surface(color = Background, shape = RoundedCornerShape(12.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(label, color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onMinus, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, null, tint = OnSurfaceVariant)
                }
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.widthIn(min = 28.dp))
                IconButton(onClick = onPlus, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, null, tint = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PomodoroTopBar() {
    Surface(color = GlassBg) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                "SMART REVIEW",
                style = MaterialTheme.typography.titleMedium.copy(
                    brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}