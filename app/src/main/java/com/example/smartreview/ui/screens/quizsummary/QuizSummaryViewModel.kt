package com.example.smartreview.ui.screens.quizsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.quiz.QuizSessionStore
import com.example.smartreview.data.repository.GamificationServiceProvider
import com.example.smartreview.data.repository.LessonRepositoryProvider
import com.example.smartreview.data.repository.QuizRepositoryProvider
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.model.LessonType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizSummaryUiState(
    val courseId: String = "",
    val quizTitle: String = "",
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val scorePercent: Float = 0f,
    val passed: Boolean = false,
    val studyTime: String = "00:00",
    val xpEarned: Int = 0,
    val streakDays: Int = 0,
    val rewardGranted: Boolean = false,
    val rewardMessage: String? = null,
    val hasSessionData: Boolean = false,
    val isLoading: Boolean = true,
    val hasNextLesson: Boolean = false,
    val nextLessonId: String? = null,
    val nextLessonTitle: String? = null,
    val nextLessonType: LessonType? = null,
    val nextLessonQuizId: String? = null,
    val isLastLessonInModule: Boolean = false,
    val isLastModule: Boolean = false,
)

class QuizSummaryViewModel(
    private val sessionId: String,
) : ViewModel() {

    private val gamificationService = GamificationServiceProvider.default
    private val courseRepository = CourseRepositoryProvider.default
    private val lessonRepository = LessonRepositoryProvider.default
    private val quizRepository = QuizRepositoryProvider.default

    private val _uiState = MutableStateFlow(QuizSummaryUiState(isLoading = true))
    val uiState: StateFlow<QuizSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            val session = QuizSessionStore.consume(sessionId)

            if (session != null) {
                val quiz = quizRepository.getQuiz(session.quizId)
                val lessonId = session.lessonId.takeIf { it.isNotBlank() } ?: quiz?.lessonId
                val courseId = quiz?.courseId?.takeIf { it.isNotBlank() }
                    ?: resolveCourseIdFromLesson(lessonId)

                val currentLessonInfo = if (!courseId.isNullOrBlank() && !lessonId.isNullOrBlank()) {
                    getCurrentLessonInfo(courseId, lessonId)
                } else {
                    null
                }

                val nextLessonInfo = if (
                    !courseId.isNullOrBlank() &&
                    !lessonId.isNullOrBlank() &&
                    currentLessonInfo != null &&
                    !currentLessonInfo.isLastInModule
                ) {
                    getNextLessonInModule(courseId, lessonId)
                } else {
                    null
                }

                _uiState.update {
                    it.copy(
                        courseId = courseId.orEmpty(),
                        quizTitle = session.quizTitle,
                        correctCount = session.correctCount,
                        totalQuestions = session.totalQuestions,
                        scorePercent = session.scorePercent.toFloat(),
                        passed = session.passed,
                        studyTime = session.formattedStudyTime(),
                        hasSessionData = true,
                        isLoading = false,
                        hasNextLesson = nextLessonInfo?.hasNext ?: false,
                        nextLessonId = nextLessonInfo?.nextLessonId,
                        nextLessonTitle = nextLessonInfo?.nextLessonTitle,
                        nextLessonType = nextLessonInfo?.nextLessonType,
                        nextLessonQuizId = nextLessonInfo?.nextLessonQuizId,
                        isLastLessonInModule = currentLessonInfo?.isLastInModule ?: false,
                        isLastModule = currentLessonInfo?.isLastModule ?: false,
                    )
                }

                LearningProgressServiceProvider.default.markQuizCompleted(session.quizId)
                if (!lessonId.isNullOrBlank()) {
                    LearningProgressServiceProvider.default.markLessonCompleted(lessonId)
                }
                awardQuizXp(session.quizId)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
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

    private suspend fun awardQuizXp(quizId: String) {
        when (val result = gamificationService.rewardQuizComplete(quizId)) {
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
                        rewardMessage = "XP quiz đã được nhận trước đó (mỗi quiz một lần).",
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

    private suspend fun resolveCourseIdFromLesson(lessonId: String?): String {
        if (lessonId.isNullOrBlank()) return ""
        return lessonRepository.getLesson(lessonId)?.courseId.orEmpty()
    }

    companion object {
        fun provideFactory(sessionId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return QuizSummaryViewModel(sessionId) as T
                }
            }
    }
}

data class NextLessonInfo(
    val hasNext: Boolean,
    val nextLessonId: String? = null,
    val nextLessonTitle: String? = null,
    val nextLessonType: LessonType? = null,
    val nextModuleId: String? = null,
    val nextLessonQuizId: String? = null,
)

data class CurrentLessonInfo(
    val isLastInModule: Boolean,
    val isLastModule: Boolean,
    val moduleIndex: Int,
    val lessonIndex: Int,
)
