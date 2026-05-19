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
import com.example.smartreview.ui.theme.*

@Composable
fun FlashcardScreen(
    navController: NavHostController,
    vm: FlashcardViewModel = viewModel()
) {
    val state    by vm.uiState.collectAsStateWithLifecycle()
    val card      = state.cards.getOrNull(state.currentIndex) ?: return
    val progress  = (state.currentIndex + 1).toFloat() / vm.total.coerceAtLeast(1)

    Scaffold(
        containerColor = Background,
        topBar = {
            FlashcardTopBar(
                title     = state.deckTitle,
                progress  = "${state.currentIndex + 1}/${vm.total}",
                onClose   = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Progress bar ──────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 0.dp),
                color      = Primary,
                trackColor = SurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // ── Flip Card ─────────────────────────────────────────────────
            FlipCard(
                card      = card,
                isFlipped = state.isFlipped,
                onClick   = { vm.flip() },
                modifier  = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
            )

            Spacer(Modifier.weight(1f))

            // ── Bottom action bar ─────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Học lại
                ActionButton(
                    label    = "Học lại",
                    icon     = Icons.Default.Replay,
                    tint     = ErrorColor,
                    borderColor = ErrorColor.copy(0.3f),
                    bg       = SurfaceContainer,
                    modifier = Modifier.weight(1f),
                    onClick  = { vm.markRepeat() }
                )
                // Lật thẻ (Primary)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .background(
                            Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { vm.flip() }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text("Lật thẻ", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
                // Biết rồi
                ActionButton(
                    label    = "Biết rồi",
                    icon     = Icons.Default.CheckCircle,
                    tint     = Secondary,
                    borderColor = Secondary.copy(0.3f),
                    bg       = SurfaceContainer,
                    modifier = Modifier.weight(1f),
                    onClick  = { vm.markKnown() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FLIP CARD  – 3-D rotation with graphicsLayer
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FlipCard(
    card:      Flashcard,
    isFlipped: Boolean,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue   = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label         = "flip"
    )
    val isFrontVisible = rotation < 90f

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY         = rotation
                cameraDistance    = 12f * density
            }
            .clickable(onClick = onClick)
    ) {
        if (isFrontVisible) {
            // ── Front face ─────────────────────────────────────────────
            CardFace(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier            = Modifier.fillMaxSize().padding(24.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint               = Primary.copy(0.5f),
                        modifier           = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text  = buildAnnotatedString {
                            append("What is\n")
                            withStyle(SpanStyle(
                                brush      = Brush.linearGradient(listOf(GradientStart, Secondary)),
                                fontWeight = FontWeight.Bold
                            )) { append(card.keyword) }
                            append("?")
                        },
                        fontSize  = 26.sp,
                        textAlign = TextAlign.Center,
                        color     = OnSurface
                    )
                    Spacer(Modifier.height(32.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.TouchApp, null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Chạm để lật thẻ", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else {
            // ── Back face (mirrored 180°) ───────────────────────────────
            CardFace(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier            = Modifier.fillMaxSize().padding(24.dp)
                ) {
                    Text(
                        text      = card.keyword,
                        fontSize  = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color     = Secondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text      = card.answer,
                        fontSize  = 16.sp,
                        color     = OnSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFace(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) { content() }
}

@Composable
private fun ActionButton(
    label:       String,
    icon:        androidx.compose.ui.graphics.vector.ImageVector,
    tint:        Color,
    borderColor: Color,
    bg:          Color,
    modifier:    Modifier = Modifier,
    onClick:     () -> Unit
) {
    Surface(
        color    = bg,
        shape    = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(80.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = tint, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun FlashcardTopBar(title: String, progress: String, onClose: () -> Unit) {
    Surface(color = GlassBg) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
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