package com.example.smartreview.ui.screens.community

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.model.ChatRoom
import com.example.smartreview.data.model.RoomIconType
import com.example.smartreview.ui.auth.AuthRoutes
import com.example.smartreview.ui.components.AuthRequiredBanner
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.screens.leaderboard.LEADERBOARD_ROUTE
import com.example.smartreview.ui.theme.*

// ─── Routes ──────────────────────────────────────────────────────────────────
const val COMMUNITY_ROOMS_ROUTE = "community_rooms"
const val CHAT_ROOM_ROUTE       = "chat_room/{roomId}"
fun chatRoomRoute(roomId: String) = "chat_room/$roomId"

// ─── Screen ──────────────────────────────────────────────────────────────────
@Composable
fun CommunityRoomsScreen(
    navController: NavHostController,
    vm: CommunityViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        topBar         = { CommunityTopBar(navController) },
        bottomBar      = { SmartReviewBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick           = {},
                shape             = RoundedCornerShape(20.dp),
                containerColor    = Color.Transparent,
                contentColor      = Color.White,
                modifier          = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
            ) {
                Icon(Icons.Default.AddComment, contentDescription = "New Room")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding      = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Hero card with filter tabs ────────────────────────────────
            item {
                HeroCard(
                    selectedFilter = state.selectedFilter,
                    filters        = state.filters,
                    onFilterSelect = { vm.onFilterSelect(it) },
                    modifier       = Modifier.padding(16.dp),
                )
            }

            state.authRequiredMessage?.let { message ->
                item {
                    AuthRequiredBanner(
                        message = message,
                        actionLabel = "Đăng nhập",
                        onAction = {
                            navController.navigate(AuthRoutes.GRAPH) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // ── Search bar ────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value          = state.searchQuery,
                    onValueChange  = { vm.onSearchChange(it) },
                    placeholder    = { Text("Tìm kiếm nhóm, tin nhắn...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon    = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
                    singleLine     = true,
                    shape          = RoundedCornerShape(16.dp),
                    colors         = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = GlassBorder,
                        focusedBorderColor   = Primary,
                        unfocusedContainerColor = SurfaceContainer,
                        focusedContainerColor   = SurfaceContainer,
                        unfocusedTextColor   = OnSurface,
                        focusedTextColor     = OnSurface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // ── Room list ─────────────────────────────────────────────────
            items(state.filteredRooms, key = { it.id }) { room ->
                RoomItem(
                    room    = room,
                    onClick = { navController.navigate(chatRoomRoute(room.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // ── Suggested section ─────────────────────────────────────────
            item {
                Text(
                    "Gợi ý cho bạn",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface,
                    modifier   = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
                )
            }

            // Featured suggested room (full width)
            if (state.suggestedRooms.isNotEmpty()) {
                item {
                    SuggestedFeaturedCard(
                        room     = state.suggestedRooms.first(),
                        onClick  = {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            // Small suggested rooms
            if (state.suggestedRooms.size > 1) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        state.suggestedRooms.drop(1).forEach { room ->
                            SuggestedSmallCard(
                                room     = room,
                                onClick  = {},
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // ── Leaderboard shortcut ──────────────────────────────────────
            item {
                OutlinedButton(
                    onClick  = { navController.navigate(LEADERBOARD_ROUTE) },
                    shape    = RoundedCornerShape(16.dp),
                    border   = BorderStroke(1.dp, Primary.copy(0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Tertiary)
                    Spacer(Modifier.width(8.dp))
                    Text("Xem Bảng Xếp Hạng", color = Primary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CommunityTopBar(navController: NavHostController) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, Primary.copy(0.4f), CircleShape)
                ) {
                    AsyncImage(
                        model = "https://picsum.photos/seed/user/100/100",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "SMART REVIEW",
                    style = MaterialTheme.typography.titleMedium.copy(
                        brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
            Box {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, null, tint = Primary)
                }
                // Red dot badge
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(ErrorColor)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(
    selectedFilter: String,
    filters:        List<String>,
    onFilterSelect: (String) -> Unit,
    modifier:       Modifier = Modifier,
) {
    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cộng đồng", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(Modifier.height(4.dp))
            Text(
                "Kết nối cùng hàng ngàn học viên, chia sẻ kinh nghiệm và cùng nhau chinh phục kiến thức mới.",
                style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    val selected = filter == selectedFilter
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (selected) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                else Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainer))
                            )
                            .then(if (!selected) Modifier.border(1.dp, GlassBorder, RoundedCornerShape(50.dp)) else Modifier)
                            .clickable { onFilterSelect(filter) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            filter,
                            color = if (selected) Color.White else OnSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ROOM ITEM
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RoomItem(room: ChatRoom, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hasUnread = room.unreadCount > 0

    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hasUnread)
                    Modifier.border(
                        BorderStroke(1.dp, Brush.linearGradient(listOf(GradientStart.copy(0.5f), GradientEnd.copy(0.5f)))),
                        RoundedCornerShape(20.dp)
                    )
                else Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(12.dp),
        ) {
            // Active left-border indicator for unread
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.linearGradient(listOf(GradientStart, Secondary)))
                )
                Spacer(Modifier.width(8.dp))
            }

            // Room icon with online dot
            Box(modifier = Modifier.size(56.dp)) {
                Surface(
                    color    = SurfaceVariant,
                    shape    = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(roomIcon(room.iconType), null,
                            tint = roomIconTint(room.iconType), modifier = Modifier.size(26.dp))
                    }
                }
                // Online/offline dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(if (room.isOnline) Secondary else OnSurfaceVariant)
                        .border(2.dp, Background, CircleShape)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Room info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        room.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = OnSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f),
                    )
                    Text(
                        room.lastMessageTime,
                        style  = MaterialTheme.typography.labelSmall,
                        color  = if (hasUnread) Primary else OnSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text     = if (room.isCurrentUserLast) "Bạn: ${room.lastMessage.removePrefix("Bạn: ")}"
                    else room.lastMessage,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = if (hasUnread) OnSurface else OnSurfaceVariant,
                    fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    // Member avatars
                    Row {
                        room.memberAvatars.take(3).forEachIndexed { idx, url ->
                            Box(
                                modifier = Modifier
                                    .offset(x = (-8 * idx).dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Background, CircleShape)
                            ) {
                                AsyncImage(model = url, contentDescription = null,
                                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                        }
                        if (room.memberCount > 3) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier         = Modifier
                                    .offset(x = (-24).dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariant)
                                    .border(2.dp, Background, CircleShape),
                            ) {
                                Text("+${room.memberCount - 3}", fontSize = 7.sp, color = OnSurfaceVariant)
                            }
                        }
                    }

                    // Unread badge or read icon
                    if (room.unreadCount > 0) {
                        Surface(
                            color  = Secondary.copy(0.20f),
                            shape  = RoundedCornerShape(50.dp),
                        ) {
                            Text(
                                "${room.unreadCount} mới",
                                color    = Secondary,
                                style    = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    } else if (room.isCurrentUserLast) {
                        Icon(Icons.Default.DoneAll, null, tint = Primary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SUGGESTED CARDS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SuggestedFeaturedCard(room: ChatRoom, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color    = GlassBg,
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.padding(16.dp),
        ) {
            Column {
                Surface(color = Primary.copy(0.20f), shape = RoundedCornerShape(6.dp)) {
                    Text("HOT NOW", color = Primary, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(room.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = OnSurface)
                Text("${room.memberCount} thành viên đang trực tuyến",
                    style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("Tham gia", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SuggestedSmallCard(room: ChatRoom, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color    = GlassBg, shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant),
            ) {
                Icon(roomIcon(room.iconType), null, tint = Secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(room.name, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = OnSurface, maxLines = 1)
            Text(room.lastMessage, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
private fun roomIcon(type: RoomIconType): ImageVector = when (type) {
    RoomIconType.SCHOOL       -> Icons.Default.School
    RoomIconType.CODE         -> Icons.Default.Code
    RoomIconType.LANGUAGE     -> Icons.Default.Translate
    RoomIconType.PSYCHOLOGY   -> Icons.Default.Psychology
    RoomIconType.ANNOUNCEMENT -> Icons.Default.Campaign
    RoomIconType.FORUM        -> Icons.Default.Forum
    RoomIconType.GLOBE        -> Icons.Default.Language
}

private fun roomIconTint(type: RoomIconType): Color = when (type) {
    RoomIconType.SCHOOL       -> SecondaryDim
    RoomIconType.CODE         -> Tertiary
    RoomIconType.LANGUAGE     -> Primary
    RoomIconType.PSYCHOLOGY   -> ErrorColor
    RoomIconType.ANNOUNCEMENT -> GradientStart
    else                      -> Primary
}