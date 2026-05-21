package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.mock.MockLessonData
import com.example.smartreview.data.model.LessonContent
import com.example.smartreview.data.repository.LessonRepository

class MockLessonRepository : LessonRepository {

    override fun getLesson(lessonId: String): LessonContent? = MockLessonData.getLesson(lessonId)

    override fun getLessonsForCourse(courseId: String): List<LessonContent> =
        MockLessonData.getLessonsForCourse(courseId)
}
