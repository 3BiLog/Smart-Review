package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreCourseRepository
// TEMPORARILY COMMENTED - Fix later when mock files are restored
// import com.example.smartreview.data.repository.mock.MockCourseRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 * [default] reads DA3-master production `courses` via [CourseFirestoreMapper].
 */
object CourseRepositoryProvider {
    val default: CourseRepository = FirestoreCourseRepository()

    // When mock is restored, use:
    // val default: CourseRepository = MockCourseRepository()
}