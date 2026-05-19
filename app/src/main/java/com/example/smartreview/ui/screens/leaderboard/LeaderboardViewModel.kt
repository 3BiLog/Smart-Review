package com.example.smartreview.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.model.LeaderboardTab
import com.example.smartreview.data.repository.LeaderboardRepository
import com.example.smartreview.data.repository.LeaderboardRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LeaderboardUiState(
    val allEntries:   List<LeaderboardEntry> = emptyList(),
    val topThree:     List<LeaderboardEntry> = emptyList(),
    val restEntries:  List<LeaderboardEntry> = emptyList(),
    val selectedTab:  LeaderboardTab         = LeaderboardTab.THIS_WEEK,
)

class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository = LeaderboardRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init { loadMockData(LeaderboardTab.THIS_WEEK) }

    fun selectTab(tab: LeaderboardTab) {
        loadMockData(tab)
    }

    private fun loadMockData(tab: LeaderboardTab) {
        // Slightly different scores per tab for realism
        val multiplier = when (tab) {
            LeaderboardTab.TODAY      -> 0.1f
            LeaderboardTab.THIS_WEEK  -> 1.0f
            LeaderboardTab.THIS_MONTH -> 4.2f
        }

        val raw = leaderboardRepository.getBaseEntries().map { entry ->
            entry.copy(score = (entry.score * multiplier).toInt())
        }

        val top3 = raw.take(3)
        // Reorder for podium display: [2nd, 1st, 3rd]
        val podium = if (top3.size >= 3) listOf(top3[1], top3[0], top3[2]) else top3

        _uiState.update {
            it.copy(
                allEntries  = raw,
                topThree    = podium,
                restEntries = raw.drop(3),
                selectedTab = tab,
            )
        }
    }
}
