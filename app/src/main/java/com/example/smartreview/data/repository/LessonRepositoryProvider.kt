package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.mock.MockLessonRepository

object LessonRepositoryProvider {
    val default: LessonRepository = MockLessonRepository()
}
