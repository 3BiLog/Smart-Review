package com.example.smartreview.data.remote.firestore

object EnrollmentFirestorePaths {
    const val ENROLLMENTS = "enrollments"

    object Fields {
        const val COURSE_ID = "courseId"
        const val USER_ID = "userId"
        const val ORDER_CODE = "orderCode"
        const val AMOUNT = "amount"
        const val TRANSACTION_ID = "transactionId"
        const val PURCHASED_AT = "purchasedAt"
    }
}
