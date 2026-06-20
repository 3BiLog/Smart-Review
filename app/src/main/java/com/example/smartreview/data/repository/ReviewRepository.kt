package com.example.smartreview.data.repository

import com.example.smartreview.data.model.Review
import com.example.smartreview.data.model.ReviewSummary
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    suspend fun getReviewsForCourse(courseId: String): List<Review>
    fun observeReviewsForCourse(courseId: String): Flow<List<Review>>
    suspend fun submitReview(review: Review): Boolean
    suspend fun updateReview(reviewId: String, content: String, rating: Int): Boolean
    suspend fun deleteReview(reviewId: String): Boolean
    suspend fun markHelpful(reviewId: String): Boolean
    suspend fun getReviewSummary(courseId: String): ReviewSummary?
    suspend fun hasUserReviewed(courseId: String, userId: String): Boolean
}