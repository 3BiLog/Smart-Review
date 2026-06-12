package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreLessonRepository

object LessonRepositoryProvider {
    val default: LessonRepository = FirestoreLessonRepository()
}
