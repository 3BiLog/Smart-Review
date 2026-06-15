package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.model.LessonContent
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.mock.MockLessonData

class MockLessonRepository : LessonRepository {

    override suspend fun getLesson(lessonId: String): LessonContent? {
        return MockLessonData.getLesson(lessonId)
    }

    override suspend fun getLessonsForCourse(courseId: String): List<LessonContent> {
        return MockLessonData.getLessonsForCourse(courseId)
    }
}