package com.example.smartreview.data.remote.firestore

import com.google.firebase.Timestamp

data class ReviewDocument(
    val id: String? = null,
    val userId: String? = null,
    val courseId: String? = null,
    val courseTitle: String? = null,
    val userName: String? = null,
    val userAvatar: String? = null,
    val rating: Long? = null,
    val content: String? = null,
    val helpfulCount: Long? = null,
    val reportCount: Long? = null,
    val status: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)