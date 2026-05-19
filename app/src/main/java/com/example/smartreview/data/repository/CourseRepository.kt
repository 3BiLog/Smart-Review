package com.example.smartreview.data.repository

import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.LessonItem

/**
 * Data access contract for the Course feature.
 * ViewModels depend on this interface — not on [com.example.smartreview.data.mock.MockCourseData].
 */
interface CourseRepository {

    fun getCourses(): List<Course>

    /** Returns the course for [courseId], or the first available course as fallback. */
    fun getCourseById(courseId: String): Course

    fun getUpNextLessons(): List<LessonItem>
}
