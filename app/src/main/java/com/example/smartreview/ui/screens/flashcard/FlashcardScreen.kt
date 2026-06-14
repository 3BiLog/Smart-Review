package com.example.smartreview.ui.screens.flashcard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.screens.flashcardsummary.flashcardSummaryRoute
import com.example.smartreview.ui.theme.*

@Composable
fun FlashcardScreen(
    navController: NavHostController,
    lessonId: String,
    vm: FlashcardViewModel = viewModel(factory = FlashcardViewModel.provideFactory(lessonId)),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    if (state.isLoading || state.cards.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    val card = state.cards[state.currentIndex]
    val progress = state.studiedCount.toFloat() / vm.total.coerceAtLeast(1)

    Scaffold(
        containerColor = Background,
        topBar = {
            FlashcardTopBar(
                title = state.deckTitle,
                progress = "${state.studiedCount}/${vm.total} · còn ${state.remainingCount}",
                onClose = { navController.popBackStack() },
            )
        },
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            if (state.isResuming) {
                ResumeBanner()
            }

            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Primary,
                trackColor = SurfaceVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { vm.previousCard() },
                    enabled = state.currentIndex > 0 && !state.isSessionComplete,
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Trước", tint = Primary)
                }
                Text(
                    "Thẻ ${state.currentIndex + 1}/${vm.total}",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
                IconButton(
                    onClick = { vm.nextCard() },
                    enabled = state.currentIndex < state.cards.lastIndex && !state.isSessionComplete,
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Sau", tint = Primary)
                }
            }

            Spacer(Modifier.height(16.dp))

            FlipCard(
                card = card,
                isFlipped = state.isFlipped,
                onClick = { vm.flip() },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f),
            )

            Spacer(Modifier.weight(1f))

            if (state.isSessionComplete) {
                Button(
                    onClick = {
                        val sessionId = vm.completeSession()
                        if (sessionId != null) {
                            navController.navigate(flashcardSummaryRoute(sessionId)) {
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hoàn thành phiên học")
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                ) {
                    ActionButton(
                        label = "Học lại",
                        icon = Icons.Default.Replay,
                        tint = ErrorColor,
                        borderColor = ErrorColor.copy(0.3f),
                        bg = SurfaceContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.markRepeat() },
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .background(
                                Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { vm.flip() },
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.height(4.dp))
                            Text("Lật thẻ", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    ActionButton(
                        label = "Biết rồi",
                        icon = Icons.Default.CheckCircle,
                        tint = Secondary,
                        borderColor = Secondary.copy(0.3f),
                        bg = SurfaceContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.markKnown() },
                    )
                }
            }
        }
    }
}

@Composable
fun FlipCard(
    card: FlashcardUiCard,
    isFlipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "flip",
    )
    val isFrontVisible = rotation < 90f

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(onClick = onClick),
    ) {
        if (isFrontVisible) {
            CardFace(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Primary.copy(0.5f),
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("What is\n")
                            withStyle(
                                SpanStyle(
                                    brush = Brush.linearGradient(listOf(GradientStart, Secondary)),
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) { append(card.front) }
                            append("?")
                        },
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        color = OnSurface,
                    )
                    Spacer(Modifier.height(32.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Icon(Icons.Default.TouchApp, null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Chạm để lật thẻ", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else {
            CardFace(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                ) {
                    Text(
                        text = card.front,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = card.back,
                        fontSize = 16.sp,
                        color = OnSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFace(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) { content() }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    borderColor: Color,
    bg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(80.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = tint, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ResumeBanner() {
    Surface(
        color = Primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            "Tiếp tục phiên học trước đó",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun FlashcardTopBar(title: String, progress: String, onClose: () -> Unit) {
    Surface(color = GlassBg) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null, tint = OnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Primary, fontWeight = FontWeight.Bold)
                Text(progress, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant)
            }
        }
    }
}
