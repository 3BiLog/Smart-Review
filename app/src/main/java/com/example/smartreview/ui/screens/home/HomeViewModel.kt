package com.example.smartreview.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.model.ResumeLearningItem
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseCard(
    val id:           String,
    val title:        String,
    val subtitle:     String,
    val imageUrl:     String,
    val progress:     Float,        // 0f – 1f
    val timeLeft:     String,
)

data class RecommendedCard(
    val title:       String,
    val description: String,
    val difficulty:  String,        // "Hard" | "Medium" | "Easy"
    val iconEmoji:   String,
)

data class HomeUiState(
    val userName:       String       = "Scholar",
    val level:          Int          = 12,
    val xp:             Int          = 1250,
    val streak:         Int          = 5,
    val goalProgress:   Float        = 0.6f,   // 15/25 min
    val goalCurrent:    Int          = 15,
    val goalTarget:     Int          = 25,
    val continueCourses: List<CourseCard>    = emptyList(),
    val resumeLearning: List<ResumeLearningItem> = emptyList(),
    val recommended:    List<RecommendedCard> = emptyList(),
)

class HomeViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
        observeGamificationProfile()
        observeResumeLearning()
    }

    private fun observeResumeLearning() {
        AuthSession.ensureStarted()
        viewModelScope.launch {
            AuthSession.state
                .map { it.isAuthenticated }
                .distinctUntilChanged()
                .collect { authenticated ->
                    if (!authenticated) {
                        _uiState.update { it.copy(resumeLearning = emptyList()) }
                        return@collect
                    }
                    refreshResumeLearning()
                }
        }
    }

    fun refreshResumeLearning() {
        viewModelScope.launch {
            if (AuthSession.state.value.isAuthenticated) {
                val items = LearningProgressServiceProvider.default.resumeLearningItems()
                _uiState.update { it.copy(resumeLearning = items) }
            }
        }
    }

    private fun observeGamificationProfile() {
        AuthSession.ensureStarted()
        viewModelScope.launch {
            AuthSession.state
                .map { it.isAuthenticated }
                .distinctUntilChanged()
                .flatMapLatest { authenticated ->
                    if (authenticated) userRepository.observeCurrentUserProfile()
                    else flowOf(null)
                }
                .collect { profile ->
                    profile ?: return@collect
                    _uiState.update {
                        it.copy(
                            userName = profile.displayName,
                            xp = profile.xp,
                            streak = profile.streak,
                            level = (profile.xp / 100).coerceAtLeast(1),
                        )
                    }
                }
        }
    }

    private fun loadMockData() {
        _uiState.value = HomeUiState(
            continueCourses = listOf(
                CourseCard(
                    id       = "1",
                    title    = "Advanced UI Patterns in Next.js",
                    subtitle = "Chapter 4: Suspense & Streaming",
                    imageUrl = "https://picsum.photos/seed/course1/400/200",
                    progress = 0.65f,
                    timeLeft = "45 mins left"
                ),
                CourseCard(
                    id       = "2",
                    title    = "Mastering React Hooks",
                    subtitle = "Lesson 2: useEffect Deep Dive",
                    imageUrl = "https://picsum.photos/seed/course2/400/200",
                    progress = 0.85f,
                    timeLeft = "12 mins left"
                ),
            ),
            recommended = listOf(
                RecommendedCard(
                    title       = "Data Structures & Algorithms",
                    description = "Optimize your code with advanced structural patterns.",
                    difficulty  = "Hard",
                    iconEmoji   = "💻"
                ),
                RecommendedCard(
                    title       = "Figma Prototyping",
                    description = "Create interactive and high-fidelity mockups.",
                    difficulty  = "Medium",
                    iconEmoji   = "🎨"
                ),
            )
        )
    }
}