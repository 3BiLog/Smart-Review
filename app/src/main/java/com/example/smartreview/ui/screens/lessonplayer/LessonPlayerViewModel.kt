package com.example.smartreview.ui.screens.lessonplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.learning.LearningProgressService
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.model.LessonProgressSnapshot
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.repository.LessonRepositoryProvider
import com.example.smartreview.data.video.YouTubeVideoUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LessonPlayerUiState(
    val currentLesson: LessonItem? = null,
    val upNextLessons: List<LessonItem> = emptyList(),
    val youtubeVideoId: String? = null,
    val videoError: String? = null,
    val lessonSubtitle: String = "",
    val isSaved: Boolean = false,
)

class LessonPlayerViewModel(
    private val lessonId: String,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
    private val lessonRepository: LessonRepository = LessonRepositoryProvider.default,
    private val progressService: LearningProgressService = LearningProgressServiceProvider.default,
) : ViewModel() {

    private var sessionId: String = UUID.randomUUID().toString()
    private var sessionStartedAt: Long = System.currentTimeMillis()

    private val _uiState = MutableStateFlow(LessonPlayerUiState())
    val uiState: StateFlow<LessonPlayerUiState> = _uiState.asStateFlow()

    init {
        loadLesson(lessonId)
    }

    private fun loadLesson(targetLessonId: String) {
        val allLessons = courseRepository.getUpNextLessons()
        val current = allLessons.find { it.id == targetLessonId }
            ?: allLessons.firstOrNull()
            ?: return
        val upNext = allLessons.filter { it.id != current.id }
        val content = lessonRepository.getLesson(current.id)
        val videoUrl = content?.videoUrl?.takeIf { it.isNotBlank() } ?: current.videoUrl
        val videoId = YouTubeVideoUrl.extractVideoId(videoUrl)
        val videoError = when {
            videoId != null -> null
            videoUrl.isNotBlank() -> "Liên kết video không hợp lệ."
            else -> "Bài học chưa có video."
        }

        _uiState.update {
            it.copy(
                currentLesson = current.copy(isCurrentlyPlaying = true),
                upNextLessons = upNext.map { item ->
                    if (item.id == current.id) item.copy(isCurrentlyPlaying = true)
                    else item.copy(isCurrentlyPlaying = false)
                },
                youtubeVideoId = videoId,
                videoError = videoError,
                lessonSubtitle = content?.subtitle ?: "",
            )
        }
        persistVideoProgress(targetLessonId)
    }

    private fun persistVideoProgress(lessonId: String) {
        viewModelScope.launch {
            if (progressService.isLessonCompleted(lessonId)) return@launch
            val existing = progressService.lessonSnapshotForLesson(lessonId)
            if (existing != null) {
                sessionId = existing.sessionId
                sessionStartedAt = existing.sessionStartedAt
                return@launch
            }
            progressService.saveLessonSnapshot(
                LessonProgressSnapshot(
                    lessonId = lessonId,
                    sessionId = sessionId,
                    sessionStartedAt = sessionStartedAt,
                    currentBlockIndex = 0,
                    viewedBlockIds = emptySet(),
                ),
            )
        }
    }

    fun toggleSave() = _uiState.update { it.copy(isSaved = !it.isSaved) }

    fun selectLesson(lesson: LessonItem) {
        if (lesson.isLocked) return
        loadLesson(lesson.id)
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
