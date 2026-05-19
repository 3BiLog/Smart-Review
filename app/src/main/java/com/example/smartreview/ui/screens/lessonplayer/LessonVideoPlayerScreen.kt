package com.example.smartreview.ui.screens.lessonplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.ui.theme.*

// ─── Route ───────────────────────────────────────────────────────────────────
const val LESSON_PLAYER_ROUTE    = "lesson_player/{lessonId}"
fun lessonPlayerRoute(lessonId: String) = "lesson_player/$lessonId"

// ─── Screen ──────────────────────────────────────────────────────────────────
@Composable
fun LessonVideoPlayerScreen(
    navController: NavHostController,
    lessonId:      String,
    vm: LessonPlayerViewModel = viewModel(
        factory = LessonPlayerViewModel.provideFactory(lessonId)
    ),
) {
    val state  by vm.uiState.collectAsStateWithLifecycle()
    val lesson  = state.currentLesson ?: return

    Scaffold(
        containerColor = Background,
        topBar         = {
            LessonPlayerTopBar(
                onBack = { navController.popBackStack() },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {

            // ── 1. Video player ───────────────────────────────────────────
            item {
                VideoPlayerArea(
                    thumbnailUrl     = lesson.thumbnailUrl,
                    isPlaying        = state.isPlaying,
                    showControls     = state.showControls,
                    playbackProgress = state.playbackProgress,
                    currentTime      = state.currentTime,
                    totalTime        = state.totalTime,
                    onTap            = { vm.toggleControls() },
                    onPlayPause      = { vm.togglePlayPause() },
                )
            }

            // ── 2. Lesson info ────────────────────────────────────────────
            item {
                LessonInfoCard(
                    lesson    = lesson,
                    isSaved   = state.isSaved,
                    onSave    = { vm.toggleSave() },
                    modifier  = Modifier.padding(16.dp),
                )
            }

            // ── 3. Tags ───────────────────────────────────────────────────
            item {
                LessonTagsRow(
                    tags     = listOf("#Async", "#Performance", "#Advanced"),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // ── 4. Up Next header ─────────────────────────────────────────
            item {
                Text(
                    "Up Next",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            // ── 5. Playlist ───────────────────────────────────────────────
            items(state.upNextLessons, key = { it.id }) { item ->
                PlaylistItem(
                    item     = item,
                    isCurrent = item.isCurrentlyPlaying,
                    onClick  = { vm.selectLesson(item) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VIDEO PLAYER AREA
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun VideoPlayerArea(
    thumbnailUrl:     String,
    isPlaying:        Boolean,
    showControls:     Boolean,
    playbackProgress: Float,
    currentTime:      String,
    totalTime:        String,
    onTap:            () -> Unit,
    onPlayPause:      () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable(onClick = onTap),
    ) {
        // Thumbnail
        AsyncImage(
            model              = thumbnailUrl.ifEmpty { "https://picsum.photos/seed/video/640/360" },
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Controls overlay (fade in/out)
        AnimatedVisibility(
            visible = showControls,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(0.30f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(0.80f),
                            )
                        )
                    )
            ) {
                // Centre play/pause button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(56.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                        .clickable(onClick = onPlayPause),
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                }

                // Bottom controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Progress bar
                    LinearProgressIndicator(
                        progress      = { playbackProgress },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color         = Primary,
                        trackColor    = Color.White.copy(0.3f),
                        strokeCap     = StrokeCap.Round,
                    )

                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Icon(Icons.Default.VolumeUp,
                                null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                "$currentTime / $totalTime",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                            )
                        }
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Default.Settings,
                                null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.Fullscreen,
                                null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // When controls hidden, show minimal play button in centre
        if (!showControls && !isPlaying) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LESSON INFO CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LessonInfoCard(
    lesson:   LessonItem,
    isSaved:  Boolean,
    onSave:   () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Module + title row
            Row(
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Module badge
                    Surface(
                        color = SurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            "Module 3",
                            color    = Secondary,
                            style    = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        lesson.title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = OnSurface,
                    )
                }
                // Save button
                IconButton(
                    onClick  = onSave,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainer),
                ) {
                    Icon(
                        imageVector        = if (isSaved) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                        contentDescription = "Save",
                        tint               = if (isSaved) Primary else OnSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = GlassBorder)
            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem(icon = Icons.Default.Visibility, text = "1.2k views")
                StatItem(icon = Icons.Default.AccessTime, text = lesson.formattedDuration)
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAGS ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LessonTagsRow(tags: List<String>, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier              = modifier,
    ) {
        tags.forEach { tag ->
            Surface(
                color  = SurfaceContainer,
                shape  = RoundedCornerShape(50.dp),
                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(50.dp)),
            ) {
                Text(
                    text     = tag,
                    color    = Primary,
                    style    = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PLAYLIST ITEM
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PlaylistItem(
    item:      LessonItem,
    isCurrent: Boolean,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier,
) {
    Surface(
        color    = if (isCurrent) SurfaceVariant else Background,
        shape    = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.border(1.dp, Primary.copy(0.4f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .then(
                if (item.isLocked) Modifier else Modifier.clickable(onClick = onClick)
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .padding(8.dp)
                .then(if (item.isLocked) Modifier.alpha(0.55f) else Modifier),
        ) {
            // Thumbnail
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .width(112.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant),
            ) {
                if (!item.isLocked && item.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model              = item.thumbnailUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                }

                // Left-side progress indicator for current
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(4.dp)
                            .align(Alignment.CenterStart)
                            .background(Brush.linearGradient(listOf(GradientStart, Secondary))),
                    )
                }

                // Overlay icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(if (isCurrent) 0.35f else 0.20f)),
                ) {
                    Icon(
                        imageVector        = when {
                            item.isLocked   -> Icons.Default.Lock
                            isCurrent       -> Icons.Default.PlayCircle
                            else            -> Icons.Default.PlayCircle
                        },
                        contentDescription = null,
                        tint               = when {
                            item.isLocked -> OnSurfaceVariant
                            isCurrent     -> Primary
                            else          -> Color.White.copy(0.8f)
                        },
                        modifier           = Modifier.size(24.dp),
                    )
                }

                // Duration badge
                if (!item.isLocked && item.durationSeconds > 0) {
                    Surface(
                        color    = Color.Black.copy(0.8f),
                        shape    = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                    ) {
                        Text(
                            item.formattedDuration,
                            color    = Color.White,
                            style    = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // Info
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier            = Modifier.weight(1f),
            ) {
                Text(
                    text       = item.title,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isCurrent) Primary else OnSurface,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text  = when {
                        isCurrent   -> "Now Playing"
                        item.isLocked -> "Complete previous lessons"
                        else        -> "Lesson"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) Secondary else OnSurfaceVariant,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LessonPlayerTopBar(onBack: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text(
                "SMART REVIEW",
                style = MaterialTheme.typography.titleMedium.copy(
                    brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, null, tint = Primary)
            }
        }
    }
}

// ─── Extension ───────────────────────────────────────────────────────────────
private fun Modifier.alpha(alpha: Float) = this.then(
    Modifier.graphicsLayer { this.alpha = alpha }
)