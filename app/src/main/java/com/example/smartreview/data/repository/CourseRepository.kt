package com.example.smartreview.data.repository

import com.example.smartreview.data.model.Course
import kotlinx.coroutines.flow.Flow

/**
 * Data access contract for course catalog and enrollment checks.
 *
 * Implementations read production Firestore `courses` documents through
 * [com.example.smartreview.data.remote.firestore.CourseFirestoreMapper].
 */
interface CourseRepository {
    fun getAllCourses(): Flow<List<Course>>
    suspend fun getCourseById(courseId: String): Course?
    suspend fun getCourseWithProgress(courseId: String, userId: String): Any?
    suspend fun isUserEnrolled(courseId: String, userId: String): Boolean
    suspend fun getCourseReviews(courseId: String): List<Any>
}