package com.example.smartreview.ui.screens.chatroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.MessageType
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import com.example.smartreview.data.repository.CommunityRealtimeRepository
import com.example.smartreview.data.repository.CommunityRepository
import com.example.smartreview.data.repository.CommunityRepositoryProvider
import com.example.smartreview.data.remote.firestore.UserFirestoreMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val roomName: String = "",
    val onlineCount: Int = 1284,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = true,
    val sendError: String? = null,
)

class ChatViewModel(
    private val roomId: String,
    private val communityRepository: CommunityRepository = CommunityRepositoryProvider.default,
    private val realtimeRepository: CommunityRealtimeRepository = CommunityRepositoryProvider.realtime,
    private val authRepository: AuthRepository = AuthRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadRoomName()
        observeMessagesRealtime()
    }

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text, sendError = null) }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        if (!authRepository.isAuthenticated()) {
            _uiState.update {
                it.copy(sendError = "Vui lòng đăng nhập để gửi tin nhắn lên Firestore.")
            }
            return
        }
        _uiState.update { it.copy(inputText = "", sendError = null) }
        viewModelScope.launch {
            val msg = buildOutgoingMessage(text)
            val firestoreDocId = realtimeRepository.sendMessage(roomId, msg)
            if (firestoreDocId == null) {
                _uiState.update { state ->
                    state.copy(
                        messages = mergeMessagesById(state.messages, listOf(msg)),
                        sendError = "Không gửi được tin nhắn. Kiểm tra kết nối hoặc đăng nhập lại.",
                    )
                }
            }
            // On success: listener emits full list with stable Firestore document ids.
        }
    }

    private fun loadRoomName() {
        val roomName = communityRepository.getRoomName(roomId)
        _uiState.update { it.copy(roomName = roomName) }
    }

    /**
     * Collects Firestore snapshot updates; [callbackFlow] removes the listener when
     * this ViewModel scope is cancelled (screen leave / process death).
     */
    private fun observeMessagesRealtime() {
        viewModelScope.launch {
            realtimeRepository.observeMessages(roomId).collect { messages ->
                _uiState.update { it.copy(messages = messages.distinctBy { msg -> msg.id }) }
            }
        }
    }

    private fun mergeMessagesById(
        current: List<ChatMessage>,
        incoming: List<ChatMessage>,
    ): List<ChatMessage> = (current + incoming).distinctBy { it.id }

    private fun buildOutgoingMessage(text: String): ChatMessage {
        val authUser = authRepository.getCurrentUser()
        val senderId = authUser?.uid ?: "me"
        val email = authUser?.email.orEmpty()
        val senderName = if (email.isNotBlank()) {
            UserFirestoreMapper.defaultDisplayName(email, senderId)
        } else {
            "Bạn"
        }
        val senderAvatar = UserFirestoreMapper.defaultAvatarUrl(senderId)
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            senderName = senderName,
            senderAvatar = senderAvatar,
            content = text,
            time = "Now",
            type = MessageType.TEXT,
            isCurrentUser = true,
        )
    }

    companion object {
        fun provideFactory(roomId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ChatViewModel(roomId) as T
            }
    }
}
