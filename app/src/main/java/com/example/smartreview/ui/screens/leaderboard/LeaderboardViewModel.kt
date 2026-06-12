package com.example.smartreview.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.model.LeaderboardTab
import com.example.smartreview.data.repository.LeaderboardRepository
import com.example.smartreview.data.repository.LeaderboardRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val allEntries: List<LeaderboardEntry> = emptyList(),
    val topThree: List<LeaderboardEntry> = emptyList(),
    val restEntries: List<LeaderboardEntry> = emptyList(),
    val selectedTab: LeaderboardTab = LeaderboardTab.THIS_WEEK,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
)

class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository = LeaderboardRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private var rawEntries: List<LeaderboardEntry> = emptyList()

    init {
        AuthSession.ensureStarted()
        viewModelScope.launch {
            AuthSession.state.collect { session ->
                _uiState.update { it.copy(isAuthenticated = session.isAuthenticated) }
                if (!session.isAuthenticated) clearLeaderboard()
            }
        }
        observeLeaderboardRealtime()
    }

    fun selectTab(tab: LeaderboardTab) {
        publishScaledEntries(rawEntries, tab)
    }

    private fun observeLeaderboardRealtime() {
        viewModelScope.launch {
            AuthSession.state
                .map { it.isAuthenticated }
                .distinctUntilChanged()
                .flatMapLatest { authenticated ->
                    if (authenticated) leaderboardRepository.observeLeaderboard()
                    else flowOf(emptyList())
                }
                .collect { entries ->
                    rawEntries = entries
                    publishScaledEntries(entries, _uiState.value.selectedTab)
                }
        }
    }

    private fun clearLeaderboard() {
        rawEntries = emptyList()
        _uiState.update {
            it.copy(
                isLoading = false,
                allEntries = emptyList(),
                topThree = emptyList(),
                restEntries = emptyList(),
            )
        }
    }

    private fun publishScaledEntries(base: List<LeaderboardEntry>, tab: LeaderboardTab) {
        val multiplier = tabMultiplier(tab)
        // FIXED: Handle empty list and get max score as Long
        val maxBaseScore = base.maxOfOrNull { it.score }?.coerceAtLeast(1) ?: 1L
        // FIXED: Convert Long to Float for scaling, then back to Long
        val scaled = base.map { entry ->
            // FIXED: Multiply Long by Float, convert to Long
            val scaledScore = (entry.score.toFloat() * multiplier).toLong()
            val progress = scaledScore.toFloat() / (maxBaseScore.toFloat() * multiplier).coerceAtLeast(1f)
            entry.copy(
                score = scaledScore,
                progress = progress,
            )
        }
        val top3 = scaled.take(3)
        val podium = if (top3.size >= 3) listOf(top3[1], top3[0], top3[2]) else top3
        _uiState.update {
            it.copy(
                allEntries = scaled,
                topThree = podium,
                restEntries = scaled.drop(3),
                selectedTab = tab,
                isLoading = false,
            )
        }
    }

    private fun tabMultiplier(tab: LeaderboardTab): Float = when (tab) {
        LeaderboardTab.TODAY -> 0.1f
        LeaderboardTab.THIS_WEEK -> 1.0f
        LeaderboardTab.THIS_MONTH -> 4.2f
    }
}