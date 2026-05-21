package com.example.smartreview.ui.screens.chatroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.MessageType
import com.example.smartreview.data.model.isMessageFromCurrentUser
import com.example.smartreview.data.model.withCurrentUserOwnership
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import com.example.smartreview.data.repository.CommunityRealtimeRepository
import com.example.smartreview.data.repository.CommunityRepository
import com.example.smartreview.data.repository.CommunityRepositoryProvider
import com.example.smartreview.data.remote.firestore.UserFirestoreMapper
import com.example.smartreview.data.util.ChatTimeFormatter
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
    val deleteError: String? = null,
    val pendingDeleteMessageId: String? = null,
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
        AuthSession.ensureStarted()
        loadRoomName()
        observeMessagesRealtime()
        observeAuthSessionForOwnership()
    }

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text, sendError = null) }

    fun requestDeleteMessage(messageId: String) {
        if (!canDeleteMessage(messageId)) return
        _uiState.update { it.copy(pendingDeleteMessageId = messageId, deleteError = null) }
    }

    fun dismissDeleteRequest() = _uiState.update { it.copy(pendingDeleteMessageId = null) }

    fun confirmDeleteMessage() {
        val messageId = _uiState.value.pendingDeleteMessageId ?: return
        if (!canDeleteMessage(messageId)) {
            dismissDeleteRequest()
            return
        }
        if (!authRepository.isAuthenticated()) {
            _uiState.update {
                it.copy(
                    pendingDeleteMessageId = null,
                    deleteError = "Vui lòng đăng nhập để xóa tin nhắn.",
                )
            }
            return
        }
        _uiState.update { it.copy(pendingDeleteMessageId = null, deleteError = null) }
        viewModelScope.launch {
            val deleted = realtimeRepository.deleteMessage(roomId, messageId)
            if (!deleted) {
                _uiState.update {
                    it.copy(deleteError = "Không xóa được tin nhắn. Chỉ có thể xóa tin nhắn của bạn.")
                }
            }
        }
    }

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
                        messages = mergeMessagesById(
                            state.messages,
                            listOf(msg.withCurrentUserOwnership(currentUserId())),
                        ),
                        sendError = "Không gửi được tin nhắn. Kiểm tra kết nối hoặc đăng nhập lại.",
                    )
                }
            }
        }
    }

    private fun loadRoomName() {
        val roomName = communityRepository.getRoomName(roomId)
        _uiState.update { it.copy(roomName = roomName) }
    }

    private fun observeMessagesRealtime() {
        viewModelScope.launch {
            realtimeRepository.observeMessages(roomId).collect { messages ->
                _uiState.update {
                    it.copy(messages = applyOwnership(messages).distinctBy { msg -> msg.id })
                }
            }
        }
    }

    /** Re-label bubbles when login/logout changes without waiting for a new Firestore snapshot. */
    private fun observeAuthSessionForOwnership() {
        viewModelScope.launch {
            AuthSession.state.collect {
                _uiState.update { state ->
                    state.copy(messages = applyOwnership(state.messages))
                }
            }
        }
    }

    private fun currentUserId(): String? = authRepository.getCurrentUser()?.uid

    private fun canDeleteMessage(messageId: String): Boolean {
        val message = _uiState.value.messages.find { it.id == messageId } ?: return false
        return message.isCurrentUser &&
            isMessageFromCurrentUser(message.senderId, currentUserId())
    }

    private fun applyOwnership(messages: List<ChatMessage>): List<ChatMessage> =
        messages.withCurrentUserOwnership(currentUserId())

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
        val createdAt = System.currentTimeMillis()
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            senderName = senderName,
            senderAvatar = senderAvatar,
            content = text,
            time = ChatTimeFormatter.format(createdAt),
            type = MessageType.TEXT,
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
