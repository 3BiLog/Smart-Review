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
import com.example.smartreview.data.repository.EnrollmentRepository
import com.example.smartreview.data.repository.EnrollmentRepositoryProvider
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
    val isEnrolled: Boolean = false,
    val isCheckingEnrollment: Boolean = true,
)

class CourseDetailViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
    private val enrollmentRepository: EnrollmentRepository = EnrollmentRepositoryProvider.default,
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

    fun refreshEnrollment() {
        viewModelScope.launch { checkEnrollmentAndApply() }
    }

    private fun loadCourseWithProgression() {
        viewModelScope.launch {
            val template = courseRepository.getCourseById(courseId)
            _uiState.update { it.copy(isLoading = true) }
            checkEnrollmentAndApply(template)
        }
    }

    private fun observeAuthForProgressRefresh() {
        AuthSession.ensureStarted()
        viewModelScope.launch {
            AuthSession.state
                .map { it.isAuthenticated to it.uid }
                .distinctUntilChanged()
                .collect {
                    refreshEnrollment()
                    refreshProgression()
                }
        }
    }

    private suspend fun checkEnrollmentAndApply(template: Course? = _uiState.value.course) {
        val course = template ?: courseRepository.getCourseById(courseId)
        if (course == null) {
            _uiState.update { it.copy(isLoading = false, isCheckingEnrollment = false) }
            return
        }

        val isFree = course.price == 0L
        val uid = AuthSession.state.value.uid
        val enrolled = isFree || (
            !uid.isNullOrBlank() && enrollmentRepository.isEnrolled(uid, courseId)
            )

        _uiState.update {
            it.copy(
                isEnrolled = enrolled,
                isCheckingEnrollment = false,
            )
        }
        applyProgression(course, enrolled)
    }

    private suspend fun applyProgressionToCourse() {
        val template = _uiState.value.course
            ?: courseRepository.getCourseById(courseId)
        val enrolled = _uiState.value.isEnrolled || (template?.price == 0L)
        applyProgression(template, enrolled)
    }

    private suspend fun applyProgression(template: Course?, isEnrolled: Boolean) {
        if (template == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        val snapshot = buildProgressSnapshot()
        val result = progressionPolicy.applyToCourse(template, snapshot)
        val lockedCourse = applyPurchaseLock(result.course, isEnrolled)
        val firstUnlockedModuleId = lockedCourse.modules.firstOrNull { !it.isLocked }?.id
        _uiState.update { state ->
            state.copy(
                course = lockedCourse,
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

    private fun applyPurchaseLock(course: Course, isEnrolled: Boolean): Course {
        if (course.price == 0L || isEnrolled) return course
        val modules = course.modules.map { module ->
            val lessons = module.lessons.map { it.copy(isLocked = true) }
            module.copy(lessons = lessons, isLocked = lessons.all { it.isLocked })
        }
        return course.copy(modules = modules)
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
