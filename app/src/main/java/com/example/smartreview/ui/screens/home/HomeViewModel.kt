package com.example.smartreview.ui.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val recommended:    List<RecommendedCard> = emptyList(),
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadMockData() }

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