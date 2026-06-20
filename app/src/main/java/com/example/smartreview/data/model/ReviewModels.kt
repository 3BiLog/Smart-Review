package com.example.smartreview.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Review(
    @get:PropertyName("id")
    val id: String = "",

    @get:PropertyName("userId")
    val userId: String = "",

    @get:PropertyName("courseId")
    val courseId: String = "",

    @get:PropertyName("courseTitle")
    val courseTitle: String = "",

    @get:PropertyName("userName")
    val userName: String = "",

    @get:PropertyName("userAvatar")
    val userAvatar: String? = null,

    @get:PropertyName("rating")
    val rating: Int = 0, // 1-5

    @get:PropertyName("content")
    val content: String = "",

    @get:PropertyName("helpfulCount")
    val helpfulCount: Long = 0,

    @get:PropertyName("reportCount")
    val reportCount: Long = 0,

    @get:PropertyName("status")
    val status: String = "visible", // visible, hidden, reported

    @get:PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @get:PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),
)

data class CreateReviewRequest(
    val courseId: String,
    val rating: Int,
    val content: String,
)

data class ReviewSummary(
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val ratingDistribution: Map<Int, Int> = emptyMap(), // rating -> count
)