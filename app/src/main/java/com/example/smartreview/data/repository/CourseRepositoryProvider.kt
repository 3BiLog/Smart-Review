package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreCourseRepository

object CourseRepositoryProvider {
    val default: CourseRepository = FirestoreCourseRepository()

}