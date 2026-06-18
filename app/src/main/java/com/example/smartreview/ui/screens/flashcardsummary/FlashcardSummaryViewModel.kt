package com.example.smartreview.ui.screens.flashcardsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.flashcard.FlashcardSessionStore
import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.model.FlashcardSessionResult
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.GamificationServiceProvider
import com.example.smartreview.data.repository.LessonRepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashcardSummaryUiState(
    val courseId: String = "",
    val xpEarned: Int = 0,
    val accuracy: Float = 0f,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val streakDays: Int = 0,
    val studyTime: String = "00:00",
    val animatedAccuracy: Float = 0f,
    val isNavigating: Boolean = false,
    val rewardGranted: Boolean = false,
    val rewardMessage: String? = null,
    val hasSessionData: Boolean = false,
    val hasNextLesson: Boolean = false,
    val nextLessonId: String? = null,
    val nextLessonTitle: String? = null,
    val nextLessonType: LessonType? = null,
    val nextLessonQuizId: String? = null,
    val isLastLessonInModule: Boolean = false,
    val isLastModule: Boolean = false,
)

class FlashcardSummaryViewModel(
    private val sessionId: String,
) : ViewModel() {

    private val gamificationService = GamificationServiceProvider.default
    private val courseRepository = CourseRepositoryProvider.default
    private val lessonRepository = LessonRepositoryProvider.default

    private val _uiState = MutableStateFlow(FlashcardSummaryUiState())
    val uiState: StateFlow<FlashcardSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
    }

    fun onPrimaryAction(onNavigate: () -> Unit) {
        if (_uiState.value.isNavigating) return
        _uiState.update { it.copy(isNavigating = true) }
        viewModelScope.launch {
            delay(200)
            onNavigate()
            _uiState.update { it.copy(isNavigating = false) }
        }
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            val session = FlashcardSessionStore.consume(sessionId)
            if (session == null) return@launch

            val lessonId = session.lessonId
            val courseId = session.courseId.takeIf { it.isNotBlank() }
                ?: resolveCourseIdFromLesson(lessonId)

            val currentLessonInfo = if (courseId.isNotBlank() && lessonId.isNotBlank()) {
                getCurrentLessonInfo(courseId, lessonId)
            } else {
                null
            }

            val nextLessonInfo = if (
                courseId.isNotBlank() &&
                lessonId.isNotBlank() &&
                currentLessonInfo != null &&
                !currentLessonInfo.isLastInModule
            ) {
                getNextLessonInModule(courseId, lessonId)
            } else {
                null
            }

            applySession(session)
            _uiState.update {
                it.copy(
                    courseId = courseId,
                    hasNextLesson = nextLessonInfo?.hasNext ?: false,
                    nextLessonId = nextLessonInfo?.nextLessonId,
                    nextLessonTitle = nextLessonInfo?.nextLessonTitle,
                    nextLessonType = nextLessonInfo?.nextLessonType,
                    nextLessonQuizId = nextLessonInfo?.nextLessonQuizId,
                    isLastLessonInModule = currentLessonInfo?.isLastInModule ?: false,
                    isLastModule = currentLessonInfo?.isLastModule ?: false,
                )
            }

            if (lessonId.isNotBlank()) {
                LearningProgressServiceProvider.default.markLessonCompleted(lessonId)
            }
            awardFlashcardXp(session.sessionId)
            animateAccuracyRing()
        }
    }

    private suspend fun getNextLessonInModule(courseId: String, currentLessonId: String): NextLessonInfo? {
        val course = courseRepository.getCourseById(courseId) ?: return null

        for (module in course.modules) {
            val lessonIndex = module.lessons.indexOfFirst { it.id == currentLessonId }
            if (lessonIndex < 0) continue

            val nextIndex = lessonIndex + 1
            if (nextIndex >= module.lessons.size) {
                return NextLessonInfo(hasNext = false)
            }

            val nextLesson = module.lessons[nextIndex]
            return NextLessonInfo(
                hasNext = true,
                nextLessonId = nextLesson.id,
                nextLessonTitle = nextLesson.title.ifBlank { "Bài học tiếp theo" },
                nextLessonType = nextLesson.lessonType,
                nextModuleId = module.id,
                nextLessonQuizId = nextLesson.quizId ?: nextLesson.id,
            )
        }

        return NextLessonInfo(hasNext = false)
    }

    private suspend fun getCurrentLessonInfo(courseId: String, currentLessonId: String): CurrentLessonInfo? {
        val course = courseRepository.getCourseById(courseId) ?: return null

        for (moduleIndex in course.modules.indices) {
            val module = course.modules[moduleIndex]
            val lessonIndex = module.lessons.indexOfFirst { it.id == currentLessonId }
            if (lessonIndex < 0) continue

            return CurrentLessonInfo(
                isLastInModule = lessonIndex == module.lessons.size - 1,
                isLastModule = moduleIndex == course.modules.size - 1,
                moduleIndex = moduleIndex,
                lessonIndex = lessonIndex,
            )
        }

        return null
    }

    private suspend fun resolveCourseIdFromLesson(lessonId: String): String {
        if (lessonId.isBlank()) return ""
        return lessonRepository.getLesson(lessonId)?.courseId.orEmpty()
    }

    private fun applySession(session: FlashcardSessionResult) {
        _uiState.update {
            it.copy(
                accuracy = session.accuracy,
                knownCount = session.knownCount,
                reviewCount = session.reviewCount,
                studyTime = session.formattedStudyTime(),
                hasSessionData = true,
            )
        }
    }

    private fun awardFlashcardXp(rewardSessionId: String) {
        viewModelScope.launch {
            when (val result = gamificationService.rewardFlashcardSession(rewardSessionId)) {
                is GamificationRewardResult.Success -> {
                    _uiState.update {
                        it.copy(
                            xpEarned = result.xpAwarded,
                            streakDays = result.newStreak,
                            rewardGranted = true,
                            rewardMessage = null,
                        )
                    }
                }
                is GamificationRewardResult.AlreadyProcessed -> {
                    _uiState.update {
                        it.copy(
                            rewardGranted = false,
                            rewardMessage = "XP flashcard đã được nhận trước đó cho phiên này.",
                        )
                    }
                }
                is GamificationRewardResult.Failed -> {
                    _uiState.update {
                        it.copy(
                            rewardGranted = false,
                            rewardMessage = "Không thể cộng XP lên Firestore. Thử lại sau.",
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    private fun animateAccuracyRing() {
        viewModelScope.launch {
            val target = _uiState.value.accuracy
            val steps = 60
            val delayMs = 900L / steps
            repeat(steps) { i ->
                delay(delayMs)
                _uiState.update { it.copy(animatedAccuracy = target * (i + 1f) / steps) }
            }
            _uiState.update { it.copy(animatedAccuracy = target) }
        }
    }

    companion object {
        fun provideFactory(sessionId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FlashcardSummaryViewModel(sessionId) as T
            }
    }
}

private data class NextLessonInfo(
    val hasNext: Boolean,
    val nextLessonId: String? = null,
    val nextLessonTitle: String? = null,
    val nextLessonType: LessonType? = null,
    val nextModuleId: String? = null,
    val nextLessonQuizId: String? = null,
)

private data class CurrentLessonInfo(
    val isLastInModule: Boolean,
    val isLastModule: Boolean,
    val moduleIndex: Int,
    val lessonIndex: Int,
)
