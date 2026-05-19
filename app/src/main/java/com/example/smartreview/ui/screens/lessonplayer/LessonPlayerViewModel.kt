package com.example.smartreview.ui.screens.lessonplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LessonPlayerUiState(
    val currentLesson:    LessonItem?      = null,
    val upNextLessons:    List<LessonItem> = emptyList(),
    val isPlaying:        Boolean          = false,
    val playbackProgress: Float            = 0.40f,  // 40 % played
    val currentTime:      String           = "04:20",
    val totalTime:        String           = "10:45",
    val isSaved:          Boolean          = false,
    val showControls:     Boolean          = true,
)

class LessonPlayerViewModel(
    private val lessonId: String,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonPlayerUiState())
    val uiState: StateFlow<LessonPlayerUiState> = _uiState.asStateFlow()

    init { loadLesson() }

    private fun loadLesson() {
        val allLessons = courseRepository.getUpNextLessons()
        val current    = allLessons.find { it.id == lessonId } ?: allLessons.first()
        val upNext     = allLessons.filter { it.id != current.id }
        _uiState.update {
            it.copy(
                currentLesson = current.copy(isCurrentlyPlaying = true),
                upNextLessons = upNext,
                totalTime     = current.formattedDuration.takeIf { d -> d != "00:00" } ?: "10:45",
            )
        }
    }

    fun togglePlayPause()  = _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    fun toggleControls()   = _uiState.update { it.copy(showControls = !it.showControls) }
    fun toggleSave()       = _uiState.update { it.copy(isSaved = !it.isSaved) }

    fun selectLesson(lesson: LessonItem) {
        if (lesson.isLocked) return
        val upNext = _uiState.value.upNextLessons
            .map { if (it.id == lesson.id) it.copy(isCurrentlyPlaying = true) else it.copy(isCurrentlyPlaying = false) }
        _uiState.update {
            it.copy(
                currentLesson = lesson.copy(isCurrentlyPlaying = true),
                upNextLessons = upNext,
                isPlaying     = false,
                playbackProgress = 0f,
                currentTime   = "00:00",
            )
        }
    }

    companion object {
        fun provideFactory(lessonId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LessonPlayerViewModel(lessonId) as T
            }
    }
}