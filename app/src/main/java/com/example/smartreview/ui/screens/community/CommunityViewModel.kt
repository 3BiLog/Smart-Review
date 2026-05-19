package com.example.smartreview.ui.screens.community

import androidx.lifecycle.ViewModel
import com.example.smartreview.data.model.ChatRoom
import com.example.smartreview.data.repository.CommunityRepository
import com.example.smartreview.data.repository.CommunityRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CommunityUiState(
    val rooms: List<ChatRoom> = emptyList(),
    val filteredRooms: List<ChatRoom> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Tất cả",
    val filters: List<String> = listOf("Tất cả", "Đang theo dõi", "Phổ biến"),
    val suggestedRooms: List<ChatRoom> = emptyList(),
)

class CommunityViewModel(
    private val communityRepository: CommunityRepository = CommunityRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init { loadMockData() }

    fun onSearchChange(q: String) {
        _uiState.update { s ->
            val filtered = if (q.isBlank()) s.rooms
            else s.rooms.filter { it.name.contains(q, ignoreCase = true) }
            s.copy(searchQuery = q, filteredRooms = filtered)
        }
    }

    fun onFilterSelect(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    private fun loadMockData() {
        val rooms = communityRepository.getRooms()
        val suggested = communityRepository.getSuggestedRooms()
        _uiState.update { it.copy(rooms = rooms, filteredRooms = rooms, suggestedRooms = suggested) }
    }
}
