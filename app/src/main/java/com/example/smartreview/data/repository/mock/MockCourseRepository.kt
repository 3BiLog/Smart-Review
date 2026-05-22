package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.lesson.LessonVideoEnrichment
import com.example.smartreview.data.mock.MockCourseData
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.repository.CourseRepository

/**
 * Mock implementation backed by [MockCourseData].
 * Replace with remote/local sources when Retrofit/Room are introduced.
 */
class MockCourseRepository : CourseRepository {

    override fun getCourses(): List<Course> = MockCourseData.courses

    override fun getCourseById(courseId: String): Course {
        val course = MockCourseData.courses.find { it.id == courseId } ?: MockCourseData.courses.first()
        return course.copy(
            modules = course.modules.map { module ->
                module.copy(lessons = module.lessons.map { LessonVideoEnrichment.enrich(it) })
            },
        )
    }

    override fun getUpNextLessons(): List<LessonItem> =
        MockCourseData.upNextLessons.map { LessonVideoEnrichment.enrich(it) }
}
