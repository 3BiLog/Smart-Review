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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private var originalCourse: Course? = null

    init {
        loadCourseWithProgression()
        observeAuthForProgressRefresh()
    }

    fun refreshProgression() {
        viewModelScope.launch { applyProgression() }
    }

    fun refreshEnrollment(justPaid: Boolean = false) {
        android.util.Log.d("CourseDetailVM", "🔥 refreshEnrollment called with justPaid=$justPaid")
        viewModelScope.launch {
            if (justPaid) {
                android.util.Log.d("CourseDetailVM", "🔄 justPaid is true, reloading course from repository")
                val course = courseRepository.getCourseById(courseId)
                originalCourse = course
                if (course != null) {
                    checkEnrollmentAndApply(course, justPaid = true)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                android.util.Log.d("CourseDetailVM", "↩️ justPaid is false, checking enrollment normally")
                checkEnrollmentAndApply()
            }
        }
    }

    private fun loadCourseWithProgression() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val course = courseRepository.getCourseById(courseId)
            originalCourse = course
            if (course == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            checkEnrollmentAndApply(course)
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

    private suspend fun checkEnrollmentAndApply(
        template: Course? = null,
        justPaid: Boolean = false,
    ) {
        val course = template ?: originalCourse ?: courseRepository.getCourseById(courseId)
        if (course == null) {
            _uiState.update { it.copy(isLoading = false, isCheckingEnrollment = false) }
            return
        }

        val isFree = course.price == 0L
        val uid = AuthSession.state.value.uid
        val enrolled = if (isFree) {
            true
        } else if (uid.isNullOrBlank()) {
            false
        } else {
            if (justPaid) {
                android.util.Log.d("CourseDetailVM", "🔍 justPaid true: querying Firestore directly for enrollment")
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("enrollments")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .await()
                val isEnrolled = snapshot.documents.isNotEmpty()
                android.util.Log.d("CourseDetailVM", "📌 Firestore enrollment result: $isEnrolled")
                isEnrolled
            } else {
                android.util.Log.d("CourseDetailVM", "📦 using repository cache for enrollment")
                enrollmentRepository.isEnrolled(uid, courseId)
            }
        }

        _uiState.update {
            it.copy(
                isEnrolled = enrolled,
                isCheckingEnrollment = false,
            )
        }
        applyProgression(originalCourse ?: course, enrolled)
    }

    private suspend fun applyProgression(
        courseOverride: Course? = null,
        enrolledOverride: Boolean? = null,
    ) {
        val course = courseOverride ?: originalCourse ?: courseRepository.getCourseById(courseId) ?: return
        val isEnrolled = enrolledOverride ?: _uiState.value.isEnrolled

        val snapshot = buildProgressSnapshot()
        val result = progressionPolicy.applyToCourse(course, snapshot)
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