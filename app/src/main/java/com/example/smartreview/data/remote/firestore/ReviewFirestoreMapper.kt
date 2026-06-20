package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.Review
import com.google.firebase.Timestamp

object ReviewFirestoreMapper {
    fun toReview(documentId: String, data: Map<String, Any>?): Review? {
        if (data == null) return null
        val dto = mapToReviewDocument(data)
        return Review(
            id = documentId,
            userId = dto.userId ?: "",
            courseId = dto.courseId ?: "",
            courseTitle = dto.courseTitle ?: "",
            userName = dto.userName ?: "",
            userAvatar = dto.userAvatar,
            rating = (dto.rating ?: 0).toInt(),
            content = dto.content ?: "",
            helpfulCount = dto.helpfulCount ?: 0,
            reportCount = dto.reportCount ?: 0,
            status = dto.status ?: "visible",
            createdAt = dto.createdAt ?: Timestamp.now(),
            updatedAt = dto.updatedAt ?: Timestamp.now(),
        )
    }

    fun newReviewMap(
        reviewId: String,
        userId: String,
        courseId: String,
        courseTitle: String,
        userName: String,
        userAvatar: String?,
        rating: Int,
        content: String,
    ): Map<String, Any> {
        val now = Timestamp.now()
        return mapOf(
            ReviewFirestorePaths.Fields.ID to reviewId,
            ReviewFirestorePaths.Fields.USER_ID to userId,
            ReviewFirestorePaths.Fields.COURSE_ID to courseId,
            ReviewFirestorePaths.Fields.COURSE_TITLE to courseTitle,
            ReviewFirestorePaths.Fields.USER_NAME to userName,
            ReviewFirestorePaths.Fields.USER_AVATAR to (userAvatar ?: ""),
            ReviewFirestorePaths.Fields.RATING to rating.toLong(),
            ReviewFirestorePaths.Fields.CONTENT to content,
            ReviewFirestorePaths.Fields.HELPFUL_COUNT to 0L,
            ReviewFirestorePaths.Fields.REPORT_COUNT to 0L,
            ReviewFirestorePaths.Fields.STATUS to "visible",
            ReviewFirestorePaths.Fields.CREATED_AT to now,
            ReviewFirestorePaths.Fields.UPDATED_AT to now,
        )
    }

    private fun mapToReviewDocument(data: Map<String, Any?>): ReviewDocument =
        ReviewDocument(
            id = stringField(data, ReviewFirestorePaths.Fields.ID),
            userId = stringField(data, ReviewFirestorePaths.Fields.USER_ID),
            courseId = stringField(data, ReviewFirestorePaths.Fields.COURSE_ID),
            courseTitle = stringField(data, ReviewFirestorePaths.Fields.COURSE_TITLE),
            userName = stringField(data, ReviewFirestorePaths.Fields.USER_NAME),
            userAvatar = stringField(data, ReviewFirestorePaths.Fields.USER_AVATAR),
            rating = numberField(data, ReviewFirestorePaths.Fields.RATING),
            content = stringField(data, ReviewFirestorePaths.Fields.CONTENT),
            helpfulCount = numberField(data, ReviewFirestorePaths.Fields.HELPFUL_COUNT),
            reportCount = numberField(data, ReviewFirestorePaths.Fields.REPORT_COUNT),
            status = stringField(data, ReviewFirestorePaths.Fields.STATUS),
            createdAt = timestampField(data, ReviewFirestorePaths.Fields.CREATED_AT),
            updatedAt = timestampField(data, ReviewFirestorePaths.Fields.UPDATED_AT),
        )

    private fun stringField(data: Map<String, Any?>, key: String): String? {
        return (data[key] as? String)?.takeIf { it.isNotBlank() }
    }

    private fun numberField(data: Map<String, Any?>, key: String): Long? {
        return (data[key] as? Number)?.toLong()
    }

    private fun timestampField(data: Map<String, Any?>, key: String): Timestamp? {
        return data[key] as? Timestamp
    }
}