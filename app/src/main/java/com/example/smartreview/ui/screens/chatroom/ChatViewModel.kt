package com.example.smartreview.ui.screens.chatroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.MessageType
import com.example.smartreview.data.repository.CommunityRepository
import com.example.smartreview.data.repository.CommunityRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChatUiState(
    val roomName: String = "",
    val onlineCount: Int = 1284,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = true,
)

class ChatViewModel(
    private val roomId: String,
    private val communityRepository: CommunityRepository = CommunityRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var nextId = 100

    init { loadMockData() }

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text) }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        val msg = ChatMessage(
            id = "msg${nextId++}",
            senderId = "me",
            senderName = "Bạn",
            senderAvatar = "https://picsum.photos/seed/me/50/50",
            content = text,
            time = "Now",
            type = MessageType.TEXT,
            isCurrentUser = true,
        )
        _uiState.update { it.copy(messages = it.messages + msg, inputText = "") }
    }

    private fun loadMockData() {
        val roomName = communityRepository.getRoomName(roomId)
        val messages = communityRepository.getMessages(roomId)
        _uiState.update { it.copy(roomName = roomName, messages = messages) }
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
