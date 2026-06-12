package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.Course
import com.example.smartreview.data.remote.firestore.CourseFirestoreMapper
import com.example.smartreview.data.remote.firestore.CourseFirestorePaths
import com.example.smartreview.data.repository.CourseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import com.example.smartreview.data.repository.CourseCache

/**
 * Firestore-backed course catalog aligned with DA3-master production schema.
 *
 * Reads courses/{courseId} via [CourseFirestoreMapper] — no direct POJO deserialization.
 */
class FirestoreCourseRepository @Inject constructor(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : CourseRepository {

    override fun getAllCourses(): Flow<List<Course>> = callbackFlow {
        val query = firestore.collection(CourseFirestorePaths.COURSES)
            .whereEqualTo(CourseFirestorePaths.Fields.STATUS, "published")

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // ignore transient errors but do not close the flow
                return@addSnapshotListener
            }
            val courses = snapshot?.documents?.mapNotNull { doc ->
                CourseFirestoreMapper.toCourse(doc.id, doc.data)
            } ?: emptyList()
            // refresh cache: clear then repopulate to reflect deletes
            CourseCache.clear()
            courses.forEach { CourseCache.put(it) }
            trySend(courses).isSuccess
        }

        awaitClose { registration.remove() }
    }

    override suspend fun getCourseById(courseId: String): Course? {
        if (courseId.isBlank()) return null
        val doc = firestore.collection(CourseFirestorePaths.COURSES)
            .document(courseId)
            .get()
            .await()
        if (!doc.exists()) return null
        val course = CourseFirestoreMapper.toCourse(doc.id, doc.data)
        if (course != null) CourseCache.put(course)
        return course
    }

    override suspend fun getCourseWithProgress(courseId: String, userId: String): Any? {
        // Progress collection integration is deferred to Phase 5B.
        return getCourseById(courseId)
    }

    override suspend fun isUserEnrolled(courseId: String, userId: String): Boolean {
        val query = firestore.collection("enrollments")
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
        return !query.isEmpty
    }

    override suspend fun getCourseReviews(courseId: String): List<Any> {
        // Reviews collection integration is deferred to a later phase.
        return emptyList()
    }
}
