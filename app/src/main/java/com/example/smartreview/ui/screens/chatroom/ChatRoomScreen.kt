package com.example.smartreview.ui.screens.chatroom

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.MessageType
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatRoomScreen(
    navController: NavHostController,
    roomId:        String,
    vm: ChatViewModel = viewModel(factory = ChatViewModel.provideFactory(roomId)),
) {
    val state     by vm.uiState.collectAsStateWithLifecycle()
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()

    // Scroll to bottom when messages change
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty())
            scope.launch { listState.animateScrollToItem(state.messages.size - 1) }
    }

    Scaffold(
        modifier       = Modifier.imePadding(),
        containerColor = Background,
        topBar         = {
            ChatTopBar(
                roomName    = state.roomName,
                onlineCount = state.onlineCount,
                onBack      = { navController.popBackStack() },
            )
        },
        bottomBar = {
            ChatInputBar(
                text      = state.inputText,
                onChange  = { vm.onInputChange(it) },
                onSend    = { vm.sendMessage() },
            )
        },
    ) { padding ->
        LazyColumn(
            state          = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(state.messages, key = { it.id }) { message ->
                when (message.type) {
                    MessageType.DATE_SEPARATOR -> DateSeparator(message.content)
                    MessageType.TEXT           -> TextMessageBubble(message)
                    MessageType.IMAGE          -> ImageMessageBubble(message)
                }
            }

            // Typing indicator
            if (state.isTyping) {
                item { TypingIndicator() }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ChatTopBar(roomName: String, onlineCount: Int, onBack: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Primary)
                }
                Box {
                    AsyncImage(
                        model = "https://picsum.photos/seed/room/50/50",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .border(2.dp, Primary.copy(0.4f), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Secondary)
                            .border(2.dp, Background, CircleShape)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        roomName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Primary,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Secondary))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$onlineCount online",
                            style  = MaterialTheme.typography.labelSmall,
                            color  = Secondary,
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, null, tint = OnSurfaceVariant)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MESSAGE BUBBLES
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DateSeparator(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Surface(color = SurfaceContainer, shape = RoundedCornerShape(50.dp)) {
            Text(
                label,
                style    = MaterialTheme.typography.labelMedium,
                color    = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun TextMessageBubble(message: ChatMessage) {
    if (message.isCurrentUser) {
        // Sent – right-aligned, gradient bubble
        Column(
            horizontalAlignment = Alignment.End,
            modifier            = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                    .padding(12.dp),
            ) {
                Text(message.content, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(message.time, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Icon(Icons.Default.DoneAll, null, tint = Secondary, modifier = Modifier.size(14.dp))
            }
        }
    } else {
        // Received – left-aligned, glass bubble
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsyncImage(
                model = message.senderAvatar, contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, Primary.copy(0.3f), CircleShape),
            )
            Column {
                Text(
                    message.senderName,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                )
                Surface(
                    color    = GlassBg,
                    shape    = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)),
                ) {
                    Text(message.content, style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface, modifier = Modifier.padding(12.dp))
                }
                Text(message.time, style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            }
        }
    }
}

@Composable
private fun ImageMessageBubble(message: ChatMessage) {
    Row(
        verticalAlignment     = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AsyncImage(
            model = message.senderAvatar, contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(32.dp).clip(CircleShape),
        )
        Column(modifier = Modifier.widthIn(max = 260.dp)) {
            Text(message.senderName, style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            Surface(
                color    = GlassBg,
                shape    = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.border(1.dp, GlassBorder,
                    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)),
            ) {
                Column {
                    message.imageUrl?.let { url ->
                        AsyncImage(
                            model = url, contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp)),
                        )
                    }
                    Text(message.content, style = MaterialTheme.typography.bodySmall,
                        color = OnSurface, modifier = Modifier.padding(10.dp))
                }
            }
            Text(message.time, style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TYPING INDICATOR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val offsets = (0..2).map { i ->
        transition.animateFloat(
            initialValue  = 0f,
            targetValue   = -6f,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 450, delayMillis = i * 150),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dot$i",
        )
    }

    Row(
        verticalAlignment     = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SurfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(14.dp))
        }
        Surface(
            color    = GlassBg.copy(alpha = 0.6f),
            shape    = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.border(1.dp, GlassBorder,
                RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                offsets.forEach { offset ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .offset(y = offset.value.dp)
                            .clip(CircleShape)
                            .background(OnSurfaceVariant.copy(0.6f)),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAT INPUT BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ChatInputBar(text: String, onChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .navigationBarsPadding(),
        ) {
            IconButton(
                onClick  = {},
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer),
            ) {
                Icon(Icons.Default.Add, null, tint = OnSurfaceVariant)
            }

            OutlinedTextField(
                value          = text,
                onValueChange  = onChange,
                placeholder    = { Text("Type a message...", style = MaterialTheme.typography.bodyMedium) },
                singleLine     = false,
                maxLines       = 4,
                shape          = RoundedCornerShape(50.dp),
                trailingIcon   = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Mood, null, tint = OnSurfaceVariant)
                    }
                },
                colors         = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GlassBorder,
                    focusedBorderColor   = Primary.copy(0.5f),
                    unfocusedContainerColor = SurfaceContainer,
                    focusedContainerColor   = SurfaceContainer,
                    unfocusedTextColor   = OnSurface,
                    focusedTextColor     = OnSurface,
                ),
                modifier = Modifier.weight(1f),
            )

            // Send button
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                    .clickable(onClick = onSend),
            ) {
                Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}