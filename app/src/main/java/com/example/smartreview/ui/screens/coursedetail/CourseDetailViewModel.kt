package com.example.smartreview.ui.screens.coursedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.Review
import com.example.smartreview.data.model.ReviewSummary
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.learning.LearningProgressionPolicy
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.ReviewRepository
import com.example.smartreview.data.repository.ReviewRepositoryProvider
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
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
    // Reviews
    val reviews: List<Review> = emptyList(),
    val reviewSummary: ReviewSummary? = null,
    val userReview: Review? = null,
    val isLoadingReviews: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val reviewSubmitSuccess: Boolean = false,
    val reviewSubmitError: String? = null,
    val showReviewDialog: Boolean = false,
    val ratingInput: Int = 0,
    val reviewContentInput: String = "",
)

class CourseDetailViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
    private val progressionPolicy: LearningProgressionPolicy = LearningProgressionPolicy(),
    private val reviewRepository: ReviewRepository = ReviewRepositoryProvider.default,
    private val userRepository: UserRepository = UserRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    private var originalCourse: Course? = null
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadCourseWithProgression()
        observeAuthForProgressRefresh()
        loadReviews()
    }

    fun refreshProgression() {
        viewModelScope.launch { applyProgression() }
    }

    fun refreshEnrollment(justPaid: Boolean = false) {
        viewModelScope.launch {
            val course = originalCourse ?: courseRepository.getCourseById(courseId)
            originalCourse = course
            if (course != null) {
                checkEnrollmentAndApply(course)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
            if (justPaid) {
                // Refresh reviews after purchase (user may review after enrolling)
                loadReviews()
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
                    loadReviews()
                }
        }
    }

    private suspend fun checkEnrollmentAndApply(
        template: Course? = null,
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
            android.util.Log.d("CourseDetailVM", "🔍 Checking enrollment directly from Firestore for user $uid")
            val snapshot = firestore
                .collection("enrollments")
                .whereEqualTo("userId", uid)
                .whereEqualTo("courseId", courseId)
                .get()
                .await()
            val result = snapshot.documents.isNotEmpty()
            android.util.Log.d("CourseDetailVM", "📌 Firestore enrollment result: $result")
            result
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

    // ========== REVIEW FUNCTIONS ==========

    private fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReviews = true) }
            try {
                val reviews = reviewRepository.getReviewsForCourse(courseId)
                val summary = buildReviewSummary(reviews)
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val userReview = if (userId != null) {
                    reviews.find { it.userId == userId }
                } else null
                android.util.Log.d("CourseDetailVM", "Loaded ${reviews.size} reviews for course=$courseId")
                _uiState.update {
                    it.copy(
                        reviews = reviews,
                        reviewSummary = summary,
                        userReview = userReview,
                        isLoadingReviews = false,
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CourseDetailVM", "Error loading reviews", e)
                _uiState.update { it.copy(isLoadingReviews = false) }
            }
        }
    }

    private fun buildReviewSummary(reviews: List<Review>): ReviewSummary? {
        if (reviews.isEmpty()) return null
        return ReviewSummary(
            averageRating = reviews.map { it.rating }.average().toFloat(),
            totalReviews = reviews.size,
            ratingDistribution = reviews.groupingBy { it.rating }.eachCount(),
        )
    }

    fun submitReview(rating: Int, content: String) {
        if (rating < 1 || rating > 5 || content.isBlank()) {
            _uiState.update { it.copy(reviewSubmitError = "Vui lòng nhập đánh giá và nội dung.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewSubmitError = null) }
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isSubmittingReview = false,
                        reviewSubmitError = "Vui lòng đăng nhập để đánh giá.",
                    )
                }
                return@launch
            }

            val existingReview = _uiState.value.userReview
            val success = if (existingReview != null) {
                reviewRepository.updateReview(existingReview.id, content, rating)
            } else {
                val userProfile = userRepository.getCurrentUserProfile()
                if (userProfile == null) {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            reviewSubmitError = "Không thể lấy thông tin người dùng.",
                        )
                    }
                    return@launch
                }
                val course = _uiState.value.course
                if (course == null) {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            reviewSubmitError = "Không tìm thấy khóa học.",
                        )
                    }
                    return@launch
                }
                if (reviewRepository.hasUserReviewed(courseId, user.uid)) {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            reviewSubmitError = "Bạn đã đánh giá khóa học này rồi.",
                        )
                    }
                    loadReviews()
                    return@launch
                }
                val review = Review(
                    userId = user.uid,
                    courseId = courseId,
                    courseTitle = course.title,
                    userName = userProfile.displayName,
                    userAvatar = userProfile.avatarUrl,
                    rating = rating,
                    content = content,
                )
                reviewRepository.submitReview(review)
            }

            if (success) {
                _uiState.update {
                    it.copy(
                        isSubmittingReview = false,
                        reviewSubmitSuccess = true,
                        showReviewDialog = false,
                        ratingInput = 0,
                        reviewContentInput = "",
                    )
                }
                loadReviews()
                refreshCourse()
            } else {
                _uiState.update {
                    it.copy(
                        isSubmittingReview = false,
                        reviewSubmitError = "Không thể gửi đánh giá. Vui lòng thử lại.",
                    )
                }
            }
        }
    }

    private fun refreshCourse() {
        viewModelScope.launch {
            val course = courseRepository.getCourseById(courseId)
            originalCourse = course
            if (course != null) {
                _uiState.update { it.copy(course = course) }
            }
        }
    }
    fun onHelpfulClick(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.markHelpful(reviewId)
            _uiState.update { state ->
                val updatedReviews = state.reviews.map { review ->
                    if (review.id == reviewId) {
                        review.copy(helpfulCount = review.helpfulCount + 1)
                    } else review
                }
                state.copy(reviews = updatedReviews)
            }
        }
    }

    fun showReviewDialog() {
        _uiState.update {
            it.copy(
                showReviewDialog = true,
                ratingInput = 0,
                reviewContentInput = "",
                reviewSubmitError = null,
                reviewSubmitSuccess = false,
            )
        }
    }

    fun showEditReviewDialog() {
        val review = _uiState.value.userReview ?: return
        _uiState.update {
            it.copy(
                showReviewDialog = true,
                ratingInput = review.rating,
                reviewContentInput = review.content,
                reviewSubmitError = null,
                reviewSubmitSuccess = false,
            )
        }
    }

    fun dismissReviewDialog() {
        _uiState.update { it.copy(showReviewDialog = false, reviewSubmitError = null) }
    }

    fun setRatingInput(rating: Int) {
        _uiState.update { it.copy(ratingInput = rating) }
    }

    fun setReviewContentInput(content: String) {
        _uiState.update { it.copy(reviewContentInput = content) }
    }

    // ========== END REVIEW FUNCTIONS ==========

    companion object {
        fun provideFactory(courseId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CourseDetailViewModel(courseId) as T
            }
    }
}