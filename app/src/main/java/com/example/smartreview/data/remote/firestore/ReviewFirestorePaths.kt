package com.example.smartreview.data.remote.firestore

object ReviewFirestorePaths {
    const val REVIEWS = "reviews"

    object Fields {
        const val ID = "id"
        const val USER_ID = "userId"
        const val COURSE_ID = "courseId"
        const val COURSE_TITLE = "courseTitle"
        const val USER_NAME = "userName"
        const val USER_AVATAR = "userAvatar"
        const val RATING = "rating"
        const val CONTENT = "content"
        const val HELPFUL_COUNT = "helpfulCount"
        const val REPORT_COUNT = "reportCount"
        const val STATUS = "status"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }
}