package com.example.smartreview.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.learning.StudyTimeManager
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.UserLearningProgress
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.LearningProgressRepositoryProvider
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import com.example.smartreview.data.learning.LearningProgressionPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseCard(
    val id:           String,
    val title:        String,
    val subtitle:     String,
    val imageUrl:     String,
    val progress:     Float,
    val timeLeft:     String,
    val nextLessonId: String? = null,
)

data class RecommendedCard(
    val title:       String,
    val description: String,
    val difficulty:  String,
    val iconEmoji:   String,
)

data class HomeUiState(
    val userName: String = "Scholar",
    val level: Int = 12,
    val xp: Long = 1250,
    val streak: Long = 5,
    val goalProgress: Float = 0.6f,
    val goalBaseMinutes: Int = 15,
    val goalTarget: Int = 25,
    val dailyGoalXP: Int = 0,
    val isGoalCompleted: Boolean = false,
    val continueCourses: List<CourseCard> = emptyList(),
    val inProgressCourses: List<Course> = emptyList(),
    val completedCourses: List<Course> = emptyList(),
    val recommended: List<RecommendedCard> = emptyList(),
    val isLoading: Boolean = true,
) {
    val goalCurrent: Int
        get() = goalBaseMinutes + StudyTimeManager.totalStudyMinutes.value.toInt()
}

class HomeViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.default,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
) : ViewModel() {

    private val learningProgressRepository = LearningProgressRepositoryProvider.default
    private val progressionPolicy = LearningProgressionPolicy()

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
        observeGamificationProfile()
        loadUserCourses()
        observeStudyTime()
    }

    fun refreshResumeLearning() {
        viewModelScope.launch {
            loadUserCourses()
        }
    }

    private fun loadUserCourses() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val userId = AuthSession.currentUserId()
                if (userId == null) {
                    _uiState.update {
                        it.copy(isLoading = false, inProgressCourses = emptyList(), completedCourses = emptyList())
                    }
                    return@launch
                }

                val allCourses = courseRepository.getAllCourses().first()
                val userProgress = learningProgressRepository.load(userId)
                    ?: UserLearningProgress(uid = userId)

                val (inProgress, completed) = categorizeCourses(allCourses, userProgress)

                val continueCards = inProgress.map { course ->
                    val progress = getCourseProgress(course, userProgress)
                    CourseCard(
                        id = course.id,
                        title = course.title,
                        subtitle = course.description.take(60),
                        imageUrl = course.imageUrl,
                        progress = progress,
                        timeLeft = formatTimeLeft(progress),
                        nextLessonId = getNextLessonId(course, userProgress)
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inProgressCourses = inProgress,
                        completedCourses = completed,
                        continueCourses = continueCards,
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading courses", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        continueCourses = emptyList(),
                        inProgressCourses = emptyList(),
                        completedCourses = emptyList(),
                    )
                }
            }
        }
    }

    private suspend fun categorizeCourses(
        courses: List<Course>,
        progress: UserLearningProgress
    ): Pair<List<Course>, List<Course>> {
        val inProgress = mutableListOf<Course>()
        val completed = mutableListOf<Course>()

        val snapshot = LearningProgressionPolicy.ProgressSnapshot(
            completedLessonIds = progress.completedLessonIds,
            completedQuizIds = progress.completedQuizIds,
        )

        for (course in courses) {
            val totalLessons = course.modules.sumOf { it.lessons.size }
            if (totalLessons == 0) continue

            var completedLessonsCount = 0
            for (module in course.modules) {
                for (lesson in module.lessons) {
                    if (progressionPolicy.isLessonFullyComplete(lesson.id, snapshot)) {
                        completedLessonsCount++
                    }
                }
            }

            if (completedLessonsCount > 0 && completedLessonsCount < totalLessons) {
                inProgress.add(course)
            } else if (completedLessonsCount == totalLessons && totalLessons > 0) {
                completed.add(course)
            }
        }

        val progressMap = mutableMapOf<String, Float>()
        for (course in inProgress) {
            val total = course.modules.sumOf { it.lessons.size }
            var completed = 0
            for (module in course.modules) {
                for (lesson in module.lessons) {
                    if (progressionPolicy.isLessonFullyComplete(lesson.id, snapshot)) {
                        completed++
                    }
                }
            }
            progressMap[course.id] = if (total > 0) completed.toFloat() / total else 0f
        }

        inProgress.sortByDescending { progressMap[it.id] ?: 0f }

        return Pair(inProgress, completed)
    }

    private suspend fun getCourseProgress(course: Course, progress: UserLearningProgress): Float {
        val snapshot = LearningProgressionPolicy.ProgressSnapshot(
            completedLessonIds = progress.completedLessonIds,
            completedQuizIds = progress.completedQuizIds,
        )

        val totalLessons = course.modules.sumOf { it.lessons.size }
        if (totalLessons == 0) return 0f

        var completedLessons = 0
        for (module in course.modules) {
            for (lesson in module.lessons) {
                if (progressionPolicy.isLessonFullyComplete(lesson.id, snapshot)) {
                    completedLessons++
                }
            }
        }

        return completedLessons.toFloat() / totalLessons
    }

    private suspend fun getNextLessonId(course: Course, progress: UserLearningProgress): String? {
        val snapshot = LearningProgressionPolicy.ProgressSnapshot(
            completedLessonIds = progress.completedLessonIds,
            completedQuizIds = progress.completedQuizIds,
        )

        for (module in course.modules) {
            for (lesson in module.lessons) {
                if (!progressionPolicy.isLessonFullyComplete(lesson.id, snapshot)) {
                    return lesson.id
                }
            }
        }
        return null
    }

    private fun formatTimeLeft(progress: Float): String {
        return when {
            progress < 0.3f -> "Mới bắt đầu"
            progress < 0.6f -> "Đang học"
            progress < 0.9f -> "Sắp xong"
            else -> "Gần hoàn thành"
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
                    val xpValue = profile.xp
                    val streakValue = profile.streak
                    val levelValue = (xpValue / 100).toInt().coerceAtLeast(1)
                    val goalMinutes = profile.dailyGoal.toInt()
                    val todayMinutes = profile.todayStudyTime.toInt()
                    val goalCompleted = todayMinutes >= goalMinutes
                    val progress = if (goalMinutes > 0) todayMinutes.toFloat() / goalMinutes else 0f

                    _uiState.update {
                        it.copy(
                            userName = profile.displayName,
                            xp = xpValue,
                            streak = streakValue,
                            level = levelValue,
                            goalProgress = progress.coerceAtMost(1f),
                            goalBaseMinutes = todayMinutes,
                            goalTarget = goalMinutes,
                            dailyGoalXP = profile.dailyGoalXP.toInt(),
                            isGoalCompleted = goalCompleted,
                        )
                    }
                }
        }
    }

    private fun observeStudyTime() {
        viewModelScope.launch {
            StudyTimeManager.totalStudyMinutes.collect { _ ->
                // Chỉ cần cập nhật lại state để recompose (goalCurrent là computed property)
                _uiState.update { it.copy() }
            }
        }
    }

    private fun loadMockData() {
        _uiState.update {
            it.copy(
                recommended = listOf(
                    RecommendedCard(
                        title = "Data Structures & Algorithms",
                        description = "Optimize your code with advanced structural patterns.",
                        difficulty = "Hard",
                        iconEmoji = "💻"
                    ),
                    RecommendedCard(
                        title = "Figma Prototyping",
                        description = "Create interactive and high-fidelity mockups.",
                        difficulty = "Medium",
                        iconEmoji = "🎨"
                    ),
                )
            )
        }
    }
}