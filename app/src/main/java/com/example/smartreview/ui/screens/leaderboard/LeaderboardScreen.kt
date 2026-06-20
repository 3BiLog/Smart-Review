package com.example.smartreview.ui.screens.leaderboard

import androidx.compose.animation.core.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.model.LeaderboardTab
import com.example.smartreview.ui.auth.AuthRoutes
import com.example.smartreview.ui.components.AuthRequiredBanner
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.theme.*

const val LEADERBOARD_ROUTE = "leaderboard"

private val GoldColor   = Color(0xFFFFD770)
private val SilverColor = Color(0xFFB0AEC0)
private val BronzeColor = Color(0xFFCD7F32)

@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    vm: LeaderboardViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        topBar         = { LeaderboardTopBar(onBack = { navController.popBackStack() }) },
        bottomBar      = { SmartReviewBottomBar(navController) },
    ) { padding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {

            item {
                LiveIndicatorRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            if (!state.isAuthenticated) {
                item {
                    AuthRequiredBanner(
                        message = "Đăng nhập để xem bảng xếp hạng realtime từ Firestore.",
                        actionLabel = "Đăng nhập",
                        onAction = {
                            navController.navigate(AuthRoutes.GRAPH) { launchSingleTop = true }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            if (state.isLoading && state.isAuthenticated) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }

            item {
                TimeTabs(
                    selectedTab = state.selectedTab,
                    onSelect    = { vm.selectTab(it) },
                    modifier    = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            item {
                if (state.topThree.size >= 3) {
                    PodiumSection(
                        entries  = state.topThree,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                    )
                }
            }

            items(state.restEntries, key = { it.userId }) { entry ->
                LeaderboardRow(
                    entry    = entry,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun LeaderboardTopBar(onBack: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Bảng Xếp Hạng",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface,
                )
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, null, tint = Primary)
            }
        }
    }
}

@Composable
private fun LiveIndicatorRow(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        val pulse = rememberInfiniteTransition(label = "pulse")
        val alpha by pulse.animateFloat(
            initialValue  = 0.4f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
            label         = "pulseAlpha",
        )
        Surface(
            color  = Secondary.copy(alpha = 0.12f),
            shape  = RoundedCornerShape(50.dp),
            modifier = Modifier.border(1.dp, Secondary.copy(0.25f), RoundedCornerShape(50.dp)),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Secondary.copy(alpha = alpha)),
                )
                Text(
                    "Đang cập nhật trực tiếp",
                    style  = MaterialTheme.typography.labelSmall,
                    color  = Secondary,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Composable
private fun TimeTabs(
    selectedTab: LeaderboardTab,
    onSelect:    (LeaderboardTab) -> Unit,
    modifier:    Modifier = Modifier,
) {
    Surface(
        color    = SurfaceContainer,
        shape    = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            LeaderboardTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { onSelect(tab) },
                ) {
                    Text(
                        tab.label,
                        color      = if (selected) Color.White else OnSurfaceVariant,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(entries: List<LeaderboardEntry>, modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier.height(280.dp),
        verticalAlignment     = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        PodiumPerson(
            entry          = entries[0],
            rank           = 2,
            platformHeight = 90.dp,
            borderColor    = SilverColor,
            showCrown      = false,
            modifier       = Modifier.weight(1f),
        )
        PodiumPerson(
            entry          = entries[1],
            rank           = 1,
            platformHeight = 130.dp,
            borderColor    = GoldColor,
            showCrown      = true,
            modifier       = Modifier.weight(1f),
        )
        PodiumPerson(
            entry          = entries[2],
            rank           = 3,
            platformHeight = 70.dp,
            borderColor    = BronzeColor,
            showCrown      = false,
            modifier       = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PodiumPerson(
    entry:          LeaderboardEntry,
    rank:           Int,
    platformHeight: Dp,
    borderColor:    Color,
    showCrown:      Boolean,
    modifier:       Modifier = Modifier,
) {
    val avatarSize = if (rank == 1) 80.dp else 64.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier            = modifier.fillMaxHeight(),
    ) {
        if (showCrown) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint     = GoldColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(4.dp))
        }

        Box(modifier = Modifier.size(avatarSize)) {
            AsyncImage(
                model              = entry.avatarUrl,
                contentDescription = entry.displayName,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width  = if (rank == 1) 4.dp else 3.dp,
                        color  = borderColor,
                        shape  = CircleShape,
                    ),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(borderColor)
                    .border(2.dp, Background, CircleShape),
            ) {
                Text(
                    "$rank",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (rank == 1) Color(0xFF3B2F00) else Color.Black,
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            entry.displayName,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = if (rank == 1) FontWeight.Bold else FontWeight.SemiBold,
            color      = if (rank == 1) GoldColor else OnSurface,
            maxLines   = 1,
        )
        Text(
            "%,d XP".format(entry.score),
            style  = MaterialTheme.typography.labelSmall,
            color  = Secondary,
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(platformHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    if (rank == 1)
                        Brush.verticalGradient(listOf(GradientStart.copy(0.4f), GradientStart.copy(0.10f)))
                    else
                        Brush.verticalGradient(listOf(GlassBg, SurfaceContainer.copy(0.5f)))
                )
                .border(
                    BorderStroke(1.dp, if (rank == 1) GradientStart.copy(0.4f) else GlassBorder),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                ),
        ) {
            if (rank == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, modifier: Modifier = Modifier) {
    Surface(
        color    = if (entry.isCurrentUser) SurfaceContainer else GlassBg.copy(0.6f),
        shape    = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (entry.isCurrentUser)
                    Modifier.border(
                        BorderStroke(2.dp, Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                        RoundedCornerShape(16.dp),
                    )
                else Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(12.dp),
        ) {
            if (entry.isCurrentUser) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                "${entry.rank}",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = if (entry.isCurrentUser) Primary else OnSurfaceVariant,
                modifier   = Modifier.width(24.dp),
            )

            AsyncImage(
                model              = entry.avatarUrl,
                contentDescription = entry.displayName,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(
                        width  = if (entry.isCurrentUser) 2.dp else 1.dp,
                        color  = if (entry.isCurrentUser) Primary else GlassBorder,
                        shape  = CircleShape,
                    ),
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = if (entry.isCurrentUser) "Bạn" else entry.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                        color = if (entry.isCurrentUser) Primary else OnSurface,
                    )
                    if (entry.isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color  = GradientStart.copy(0.20f),
                            shape  = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                "Bạn",
                                color    = Primary,
                                style    = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress  = { entry.progress },
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color     = if (entry.isCurrentUser) Primary else SurfaceVariant,
                    trackColor = SurfaceVariant.copy(0.5f),
                    strokeCap  = StrokeCap.Round,
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                "%,d".format(entry.score),
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = Secondary,
            )
        }
    }
}