package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.Review
import com.example.smartreview.data.model.ReviewSummary
import com.example.smartreview.data.remote.firestore.ReviewFirestoreMapper
import com.example.smartreview.data.remote.firestore.ReviewFirestorePaths
import com.example.smartreview.data.repository.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp

class FirestoreReviewRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ReviewRepository {

    override suspend fun getReviewsForCourse(courseId: String): List<Review> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(ReviewFirestorePaths.REVIEWS)
                .whereEqualTo(ReviewFirestorePaths.Fields.COURSE_ID, courseId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                ReviewFirestoreMapper.toReview(doc.id, doc.data)
            }
                .filter { it.status == "visible" }
                .sortedByDescending { it.createdAt.seconds }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreReviewRepo", "Error getting reviews for course=$courseId", e)
            emptyList()
        }
    }

    override fun observeReviewsForCourse(courseId: String): Flow<List<Review>> = callbackFlow {
        val listener = firestore.collection(ReviewFirestorePaths.REVIEWS)
            .whereEqualTo(ReviewFirestorePaths.Fields.COURSE_ID, courseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReviewRepo", "Error observing reviews for course=$courseId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    ReviewFirestoreMapper.toReview(doc.id, doc.data)
                }
                    ?.filter { it.status == "visible" }
                    ?.sortedByDescending { it.createdAt.seconds }
                    ?: emptyList()
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun submitReview(review: Review): Boolean = withContext(Dispatchers.IO) {
        try {
            if (hasUserReviewed(review.courseId, review.userId)) {
                return@withContext false
            }
            val docRef = firestore.collection(ReviewFirestorePaths.REVIEWS).document()
            val data = ReviewFirestoreMapper.newReviewMap(
                reviewId = docRef.id,
                userId = review.userId,
                courseId = review.courseId,
                courseTitle = review.courseTitle,
                userName = review.userName,
                userAvatar = review.userAvatar,
                rating = review.rating,
                content = review.content
            )
            firestore.runTransaction { transaction ->
                transaction.set(docRef, data)
                val courseRef = firestore.collection("courses").document(review.courseId)
                transaction.update(courseRef, "ratingCount", FieldValue.increment(1))
            }.await()

            // Recalculate average rating
            updateCourseAverageRating(review.courseId)
            true
        } catch (e: Exception) {
            android.util.Log.e("FirestoreReviewRepo", "Error submitting review", e)
            false
        }
    }

    private suspend fun updateCourseAverageRating(courseId: String) {
        try {
            val reviews = getReviewsForCourse(courseId)
            if (reviews.isEmpty()) return
            val avg = reviews.map { it.rating }.average().toFloat()
            val count = reviews.size
            firestore.collection("courses").document(courseId)
                .update(
                    mapOf(
                        "rating" to avg,
                        "ratingCount" to count
                    )
                ).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreReviewRepo", "Error updating course rating", e)
        }
    }

    override suspend fun updateReview(reviewId: String, content: String, rating: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection(ReviewFirestorePaths.REVIEWS).document(reviewId)
            val snapshot = docRef.get().await()
            val courseId = snapshot.getString(ReviewFirestorePaths.Fields.COURSE_ID) ?: return@withContext false
            docRef.update(
                mapOf(
                    ReviewFirestorePaths.Fields.CONTENT to content,
                    ReviewFirestorePaths.Fields.RATING to rating.toLong(),
                    ReviewFirestorePaths.Fields.UPDATED_AT to Timestamp.now(),
                )
            ).await()
            updateCourseAverageRating(courseId)
            true
        } catch (e: Exception) {
            android.util.Log.e("FirestoreReviewRepo", "Error updating review", e)
            false
        }
    }

    override suspend fun deleteReview(reviewId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            firestore.collection(ReviewFirestorePaths.REVIEWS).document(reviewId)
                .update(ReviewFirestorePaths.Fields.STATUS, "hidden")
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun markHelpful(reviewId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            firestore.collection(ReviewFirestorePaths.REVIEWS).document(reviewId)
                .update(ReviewFirestorePaths.Fields.HELPFUL_COUNT, FieldValue.increment(1))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getReviewSummary(courseId: String): ReviewSummary? = withContext(Dispatchers.IO) {
        try {
            val reviews = getReviewsForCourse(courseId)
            if (reviews.isEmpty()) return@withContext null
            val avg = reviews.map { it.rating }.average().toFloat()
            val distribution = reviews.groupingBy { it.rating }.eachCount()
            ReviewSummary(
                averageRating = avg,
                totalReviews = reviews.size,
                ratingDistribution = distribution,
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun hasUserReviewed(courseId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(ReviewFirestorePaths.REVIEWS)
                .whereEqualTo(ReviewFirestorePaths.Fields.COURSE_ID, courseId)
                .whereEqualTo(ReviewFirestorePaths.Fields.USER_ID, userId)
                .get()
                .await()
            snapshot.documents.any { doc ->
                doc.getString(ReviewFirestorePaths.Fields.STATUS) != "hidden"
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreReviewRepo", "Error checking user review", e)
            false
        }
    }
}