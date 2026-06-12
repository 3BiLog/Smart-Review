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
import kotlinx.coroutines.flow.first
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
    val hasContentBlocks: Boolean = false,
)

class LessonPlayerViewModel(
    private val lessonId: String,
    private val courseId: String? = null,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
    private val lessonRepository: LessonRepository = LessonRepositoryProvider.default,
    private val progressService: LearningProgressService = LearningProgressServiceProvider.default,
) : ViewModel() {

    private var sessionId: String = UUID.randomUUID().toString()
    private var sessionStartedAt: Long = System.currentTimeMillis()

    private val _uiState = MutableStateFlow(LessonPlayerUiState())
    val uiState: StateFlow<LessonPlayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadLesson(lessonId) }
    }

    private suspend fun loadLesson(targetLessonId: String) {
        // Priority A: if courseId provided, load course and resolve lesson from modules[].lessons[]
        val courseFromArg = courseId?.let { cid -> courseRepository.getCourseById(cid) }
        val courseLessonsFromArg = courseFromArg?.modules?.flatMap { it.lessons }.orEmpty()
        var current: LessonItem? = courseLessonsFromArg.find { it.id == targetLessonId }
        var content: com.example.smartreview.data.model.LessonContent? = null

        // Priority B: if not found, try lessonRepository (mock or real)
        if (current == null) {
            content = lessonRepository.getLesson(targetLessonId)
            if (content != null) {
                // try to resolve siblings from content.courseId
                val siblingLessons = courseRepository.getCourseById(content.courseId)?.modules?.flatMap { it.lessons }.orEmpty()
                current = siblingLessons.find { it.id == targetLessonId }
                    ?: LessonItem(
                        id = content.id,
                        title = content.title,
                        durationSeconds = content.estimatedMinutes * 60,
                        thumbnailUrl = "",
                        isLocked = false,
                        videoUrl = content.videoUrl,
                        lessonType = com.example.smartreview.data.model.LessonType.VIDEO,
                    )
            }
        }

        // Priority C: try FirestoreLessonRepository (placeholder)
        if (current == null) {
            val fsRepo = com.example.smartreview.data.repository.firestore.FirestoreLessonRepository()
            val fsContent = fsRepo.getLesson(targetLessonId)
            if (fsContent != null) {
                content = fsContent
                current = LessonItem(
                    id = fsContent.id,
                    title = fsContent.title,
                    durationSeconds = fsContent.estimatedMinutes * 60,
                    thumbnailUrl = "",
                    isLocked = false,
                    videoUrl = fsContent.videoUrl,
                lessonType = com.example.smartreview.data.model.LessonType.VIDEO,
            )
            }
        }

        // Priority D: last-resort scan all courses
        if (current == null) {
            val allCourses = try { courseRepository.getAllCourses().first() } catch (_: Throwable) { emptyList() }
            current = allCourses
                .flatMap { it.modules }
                .flatMap { it.lessons }
                .find { it.id == targetLessonId }
        }

        if (current == null) return

        // Determine the single next lesson in progression order.
        val lessonsSequence: List<LessonItem> = when {
            courseLessonsFromArg.isNotEmpty() -> courseLessonsFromArg
            content?.courseId != null -> courseRepository.getCourseById(content.courseId!!)?.modules?.flatMap { it.lessons } ?: emptyList()
            else -> emptyList()
        }
        val nextLesson = lessonsSequence.let { seq ->
            val idx = seq.indexOfFirst { it.id == current.id }
            if (idx >= 0) seq.getOrNull(idx + 1) else null
        }
        val upNext = nextLesson?.let { listOf(it) } ?: emptyList()

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
                hasContentBlocks = content?.blocks?.isNotEmpty() ?: false,
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
        viewModelScope.launch { loadLesson(lesson.id) }
    }

    companion object {
        fun provideFactory(lessonId: String, courseId: String? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LessonPlayerViewModel(lessonId, courseId) as T
            }
    }
}
