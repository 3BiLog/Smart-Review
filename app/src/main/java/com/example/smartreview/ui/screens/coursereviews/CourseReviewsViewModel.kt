package com.example.smartreview.ui.screens.coursereviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.model.Review
import com.example.smartreview.data.model.ReviewSummary
import com.example.smartreview.data.repository.ReviewRepository
import com.example.smartreview.data.repository.ReviewRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseReviewsUiState(
    val courseTitle: String = "",
    val reviews: List<Review> = emptyList(),
    val summary: ReviewSummary? = null,
    val userReview: Review? = null,
    val isLoading: Boolean = true,
)

class CourseReviewsViewModel(
    private val courseId: String,
    courseTitle: String,
    private val reviewRepository: ReviewRepository = ReviewRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseReviewsUiState(courseTitle = courseTitle))
    val uiState: StateFlow<CourseReviewsUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val reviews = reviewRepository.getReviewsForCourse(courseId)
                val summary = buildReviewSummary(reviews)
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val userReview = if (userId != null) reviews.find { it.userId == userId } else null
                _uiState.update {
                    it.copy(
                        reviews = reviews,
                        summary = summary,
                        userReview = userReview,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CourseReviewsVM", "Error loading reviews", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onHelpfulClick(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.markHelpful(reviewId)
            _uiState.update { state ->
                state.copy(
                    reviews = state.reviews.map { review ->
                        if (review.id == reviewId) review.copy(helpfulCount = review.helpfulCount + 1)
                        else review
                    },
                    userReview = state.userReview?.let { review ->
                        if (review.id == reviewId) review.copy(helpfulCount = review.helpfulCount + 1)
                        else review
                    },
                )
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

    companion object {
        fun provideFactory(courseId: String, courseTitle: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CourseReviewsViewModel(courseId, courseTitle) as T
            }
    }
}
