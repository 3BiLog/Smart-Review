package com.example.smartreview.ui.screens.lessonplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.learning.LearningProgressService
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.model.LessonCompletionResult
import com.example.smartreview.data.lesson.LessonSessionStore
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.repository.LessonRepositoryProvider
import com.example.smartreview.data.repository.GamificationServiceProvider
import com.example.smartreview.data.gamification.GamificationRewardResult
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
    val isLoading: Boolean = true,
    val isNavigating: Boolean = false,
    val isCompleting: Boolean = false,
    val showCompleteDialog: Boolean = false,
    val selectedNextLesson: LessonItem? = null,  // ✅ Thêm selected lesson từ Up Next
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
    private var cachedLessonsSequence: List<LessonItem>? = null
    private var currentLessonIndex: Int = -1

    private val _uiState = MutableStateFlow(LessonPlayerUiState(isLoading = true))
    val uiState: StateFlow<LessonPlayerUiState> = _uiState.asStateFlow()

    init {
        android.util.Log.d("LessonPlayerViewModel", "ViewModel created: lessonId=$lessonId, courseId=$courseId")
        viewModelScope.launch { loadLesson(lessonId) }
    }

    private suspend fun loadLesson(targetLessonId: String) {
        android.util.Log.d("LessonPlayerViewModel", "loadLesson called: targetLessonId=$targetLessonId, courseId=$courseId")

        if (_uiState.value.isNavigating) {
            android.util.Log.d("LessonPlayerViewModel", "Already navigating, skipping load")
            return
        }

        if (_uiState.value.currentLesson?.id == targetLessonId && !_uiState.value.isLoading) {
            android.util.Log.d("LessonPlayerViewModel", "Already loaded, skipping")
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                currentLesson = null,
                upNextLessons = emptyList(),
                youtubeVideoId = null,
                videoError = null,
                showCompleteDialog = false,
                selectedNextLesson = null
            )
        }

        val courseFromArg = courseId?.let { cid ->
            android.util.Log.d("LessonPlayerViewModel", "Loading course with id: $cid")
            courseRepository.getCourseById(cid)
        }
        var courseLessonsFromArg = courseFromArg?.modules?.flatMap { it.lessons }.orEmpty()
        var current: LessonItem? = courseLessonsFromArg.find { it.id == targetLessonId }
        var content: com.example.smartreview.data.model.LessonContent? = null

        android.util.Log.d("LessonPlayerViewModel", "Found in courseFromArg: ${current != null}")

        if (current == null) {
            content = lessonRepository.getLesson(targetLessonId)
            android.util.Log.d("LessonPlayerViewModel", "Found in lessonRepository: ${content != null}")
            if (content != null) {
                val siblingLessons = courseRepository.getCourseById(content.courseId)?.modules?.flatMap { it.lessons }.orEmpty()
                current = siblingLessons.find { it.id == targetLessonId }
                    ?: LessonItem(
                        id = content.id,
                        title = content.title,
                        durationSeconds = content.estimatedMinutes * 60,
                        thumbnailUrl = "",
                        isLocked = false,
                        videoUrl = content.videoUrl,
                        lessonType = LessonType.VIDEO,
                    )
                if (siblingLessons.isNotEmpty()) {
                    courseLessonsFromArg = siblingLessons
                }
            }
        }

        if (current == null) {
            val fsRepo = com.example.smartreview.data.repository.firestore.FirestoreLessonRepository()
            val fsContent = fsRepo.getLesson(targetLessonId)
            android.util.Log.d("LessonPlayerViewModel", "Found in FirestoreLessonRepository: ${fsContent != null}")
            if (fsContent != null) {
                content = fsContent
                current = LessonItem(
                    id = fsContent.id,
                    title = fsContent.title,
                    durationSeconds = fsContent.estimatedMinutes * 60,
                    thumbnailUrl = "",
                    isLocked = false,
                    videoUrl = fsContent.videoUrl,
                    lessonType = LessonType.VIDEO,
                )
            }
        }

        if (current == null) {
            val allCourses = try { courseRepository.getAllCourses().first() } catch (_: Throwable) { emptyList() }
            current = allCourses
                .flatMap { it.modules }
                .flatMap { it.lessons }
                .find { it.id == targetLessonId }
            android.util.Log.d("LessonPlayerViewModel", "Found in allCourses scan: ${current != null}")
        }

        if (current == null) {
            android.util.Log.e("LessonPlayerViewModel", "Lesson not found: $targetLessonId")
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        if (cachedLessonsSequence == null || courseLessonsFromArg.isNotEmpty()) {
            cachedLessonsSequence = when {
                courseLessonsFromArg.isNotEmpty() -> courseLessonsFromArg
                content?.courseId != null -> {
                    courseRepository.getCourseById(content.courseId!!)?.modules?.flatMap { it.lessons } ?: emptyList()
                }
                else -> emptyList()
            }
        }

        val lessonsSequence = cachedLessonsSequence ?: emptyList()

        var fixedCurrent = current
        if (fixedCurrent.lessonType == LessonType.QUIZ && fixedCurrent.quizId.isNullOrBlank()) {
            fixedCurrent = fixedCurrent.copy(quizId = fixedCurrent.id)
        }

        currentLessonIndex = lessonsSequence.indexOfFirst { it.id == fixedCurrent.id }
        val nextLesson = if (currentLessonIndex >= 0 && currentLessonIndex + 1 < lessonsSequence.size) {
            var next = lessonsSequence[currentLessonIndex + 1]
            if (next.lessonType == LessonType.QUIZ && next.quizId.isNullOrBlank()) {
                next = next.copy(quizId = next.id)
            }
            next
        } else null

        val videoUrl = content?.videoUrl?.takeIf { it.isNotBlank() } ?: fixedCurrent.videoUrl
        val videoId = YouTubeVideoUrl.extractVideoId(videoUrl)
        val videoError = when {
            videoId != null -> null
            videoUrl.isNotBlank() -> "Liên kết video không hợp lệ."
            else -> "Bài học chưa có video."
        }

        _uiState.update {
            it.copy(
                currentLesson = fixedCurrent.copy(isCurrentlyPlaying = true),
                upNextLessons = nextLesson?.let { listOf(it) } ?: emptyList(),
                youtubeVideoId = videoId,
                videoError = videoError,
                lessonSubtitle = content?.subtitle ?: "",
                hasContentBlocks = content?.blocks?.isNotEmpty() ?: false,
                isLoading = false,
                isNavigating = false,
                showCompleteDialog = false,
                selectedNextLesson = null
            )
        }
        persistVideoProgress(targetLessonId)
        nextLesson?.let { preloadLessonData(it.id) }
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

    private suspend fun preloadLessonData(lessonId: String) {
        try {
            android.util.Log.d("LessonPlayerViewModel", "Preloading lesson: $lessonId")
            lessonRepository.getLesson(lessonId)
        } catch (e: Exception) {
            android.util.Log.e("LessonPlayerViewModel", "Error preloading lesson: ${e.message}")
        }
    }

    // ✅ Hàm hoàn thành bài học và chuyển đến lesson mặc định (next lesson)
    fun completeLessonAndContinue(
        onSuccess: (nextLessonId: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCompleting = true) }

                val currentLesson = _uiState.value.currentLesson
                if (currentLesson == null) {
                    onSuccess(null)
                    return@launch
                }

                val alreadyCompleted = progressService.isLessonCompleted(currentLesson.id)

                if (!alreadyCompleted) {
                    completeCurrentLesson(currentLesson.id)
                }

                val nextLessonId = getNextLessonId()

                _uiState.update {
                    it.copy(
                        showCompleteDialog = false,
                        isCompleting = false,
                        selectedNextLesson = null
                    )
                }

                onSuccess(nextLessonId)

            } catch (e: Exception) {
                android.util.Log.e("LessonPlayerViewModel", "Error completing lesson", e)
                _uiState.update {
                    it.copy(
                        showCompleteDialog = false,
                        isCompleting = false
                    )
                }
                onSuccess(null)
            }
        }
    }

    // ✅ Hàm hoàn thành bài học và chuyển đến lesson đã chọn (từ Up Next)
    fun completeLessonAndNavigateToSelected(
        onSuccess: (selectedLesson: LessonItem?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCompleting = true) }

                val currentLesson = _uiState.value.currentLesson
                val selectedLesson = _uiState.value.selectedNextLesson

                if (currentLesson == null || selectedLesson == null) {
                    onSuccess(null)
                    return@launch
                }

                val alreadyCompleted = progressService.isLessonCompleted(currentLesson.id)

                if (!alreadyCompleted) {
                    completeCurrentLesson(currentLesson.id)
                }

                _uiState.update {
                    it.copy(
                        showCompleteDialog = false,
                        isCompleting = false,
                        selectedNextLesson = null
                    )
                }

                onSuccess(selectedLesson)

            } catch (e: Exception) {
                android.util.Log.e("LessonPlayerViewModel", "Error completing lesson", e)
                _uiState.update {
                    it.copy(
                        showCompleteDialog = false,
                        isCompleting = false
                    )
                }
                onSuccess(null)
            }
        }
    }

    // ✅ Hàm chung để hoàn thành lesson và cộng XP
    private suspend fun completeCurrentLesson(lessonId: String) {
        progressService.markLessonCompleted(lessonId)
        android.util.Log.d("LessonPlayerViewModel", "Lesson marked completed: $lessonId")

        try {
            val gamificationService = GamificationServiceProvider.default
            val xpResult = gamificationService.rewardLessonComplete(lessonId)
            when (xpResult) {
                is GamificationRewardResult.Success -> {
                    android.util.Log.d("LessonPlayerViewModel", "XP earned: ${xpResult.xpAwarded}, Streak: ${xpResult.newStreak}")
                }
                is GamificationRewardResult.AlreadyProcessed -> {
                    android.util.Log.d("LessonPlayerViewModel", "XP already awarded for this lesson")
                }
                is GamificationRewardResult.Failed -> {
                    android.util.Log.e("LessonPlayerViewModel", "Failed to award XP")
                }
                else -> {
                    android.util.Log.d("LessonPlayerViewModel", "Other result type")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LessonPlayerViewModel", "Error awarding XP", e)
        }

        createLessonSummarySession(lessonId)
    }

    private suspend fun createLessonSummarySession(lessonId: String) {
        val lesson = lessonRepository.getLesson(lessonId)
        val newSessionId = UUID.randomUUID().toString()
        val completionResult = LessonCompletionResult(
            sessionId = newSessionId,
            lessonId = lessonId,
            courseId = lesson?.courseId ?: "",
            lessonTitle = lesson?.title ?: "",
            totalBlocks = 1,
            viewedBlocks = 1,
            durationMs = System.currentTimeMillis() - sessionStartedAt
        )
        LessonSessionStore.put(completionResult)
        sessionId = newSessionId
    }

    // ✅ Set selected next lesson (từ Up Next)
    fun setSelectedNextLesson(lesson: LessonItem) {
        _uiState.update { it.copy(selectedNextLesson = lesson) }
    }

    // ✅ Clear selected next lesson
    fun clearSelectedNextLesson() {
        _uiState.update { it.copy(selectedNextLesson = null) }
    }

    fun showCompleteConfirmation() {
        _uiState.update { it.copy(showCompleteDialog = true) }
    }

    fun dismissCompleteDialog() {
        _uiState.update {
            it.copy(
                showCompleteDialog = false,
                selectedNextLesson = null
            )
        }
    }

    suspend fun markLessonCompleted(lessonId: String): Boolean {
        return try {
            if (!progressService.isLessonCompleted(lessonId)) {
                progressService.markLessonCompleted(lessonId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getNextLessonId(): String? {
        return _uiState.value.upNextLessons.firstOrNull()?.id
    }

    fun hasNextLesson(): Boolean {
        return _uiState.value.upNextLessons.isNotEmpty()
    }

    fun setNavigating(isNavigating: Boolean) {
        _uiState.update { it.copy(isNavigating = isNavigating) }
    }

    fun toggleSave() = _uiState.update { it.copy(isSaved = !it.isSaved) }

    fun selectLesson(lesson: LessonItem) {
        if (lesson.isLocked) return
        if (_uiState.value.isNavigating) return
        viewModelScope.launch {
            setNavigating(true)
            loadLesson(lesson.id)
        }
    }

    fun refreshCurrentLesson() {
        _uiState.value.currentLesson?.let { lesson ->
            viewModelScope.launch { loadLesson(lesson.id) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        android.util.Log.d("LessonPlayerViewModel", "ViewModel cleared for lesson: $lessonId")
        cachedLessonsSequence = null
    }

    companion object {
        fun provideFactory(lessonId: String, courseId: String? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    android.util.Log.d("LessonPlayerViewModel", "Factory creating ViewModel: lessonId=$lessonId, courseId=$courseId")
                    return LessonPlayerViewModel(lessonId, courseId) as T
                }
            }
    }
}