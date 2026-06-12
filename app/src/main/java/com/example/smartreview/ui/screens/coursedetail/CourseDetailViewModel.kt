package com.example.smartreview.ui.screens.coursedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.learning.LearningProgressionPolicy
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: Course? = null,
    val expandedModuleIds: Set<String> = emptySet(),
    val isBookmarked: Boolean = false,
    val isLoading: Boolean = true,
    val courseProgress: Float = 0f,
    val completedLessonCount: Int = 0,
    val totalLessonCount: Int = 0,
    val recommendedNextLessonId: String? = null,
    val recommendedNextLessonTitle: String? = null,
)

class CourseDetailViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
    private val progressionPolicy: LearningProgressionPolicy = LearningProgressionPolicy(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourseWithProgression()
        observeAuthForProgressRefresh()
    }

    fun refreshProgression() {
        viewModelScope.launch { applyProgressionToCourse() }
    }

    private fun loadCourseWithProgression() {
        viewModelScope.launch {
            val template = courseRepository.getCourseById(courseId)
            _uiState.update { it.copy(isLoading = true) }
            applyProgression(template)
        }
    }

    private fun observeAuthForProgressRefresh() {
        AuthSession.ensureStarted()
        viewModelScope.launch {
            AuthSession.state
                .map { it.isAuthenticated }
                .distinctUntilChanged()
                .collect { refreshProgression() }
        }
    }

    private suspend fun applyProgressionToCourse() {
        val template = _uiState.value.course
            ?: courseRepository.getCourseById(courseId)
        applyProgression(template)
    }

    private suspend fun applyProgression(template: Course?) {
        if (template == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        val snapshot = buildProgressSnapshot()
        val result = progressionPolicy.applyToCourse(template, snapshot)
        val firstUnlockedModuleId = result.course.modules.firstOrNull { !it.isLocked }?.id
        _uiState.update { state ->
            state.copy(
                course = result.course,
                courseProgress = result.progressFraction,
                completedLessonCount = result.completedLessonCount,
                totalLessonCount = result.totalLessonCount,
                recommendedNextLessonId = result.recommendedNextLessonId,
                recommendedNextLessonTitle = result.recommendedNextLessonTitle,
                expandedModuleIds = state.expandedModuleIds.ifEmpty {
                    if (firstUnlockedModuleId != null) setOf(firstUnlockedModuleId) else emptySet()
                },
                isLoading = false,
            )
        }
    }

    private suspend fun buildProgressSnapshot(): LearningProgressionPolicy.ProgressSnapshot {
        if (!AuthSession.state.value.isAuthenticated) {
            return LearningProgressionPolicy.ProgressSnapshot()
        }
        val progress = LearningProgressServiceProvider.default.currentProgress()
            ?: return LearningProgressionPolicy.ProgressSnapshot()
        return LearningProgressionPolicy.ProgressSnapshot(
            completedLessonIds = progress.completedLessonIds,
            completedQuizIds = progress.completedQuizIds,
        )
    }

    fun toggleModule(moduleId: String) {
        _uiState.update { state ->
            val ids = state.expandedModuleIds
            state.copy(expandedModuleIds = if (moduleId in ids) ids - moduleId else ids + moduleId)
        }
    }

    fun toggleBookmark() = _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }

    companion object {
        fun provideFactory(courseId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CourseDetailViewModel(courseId) as T
            }
    }
}
