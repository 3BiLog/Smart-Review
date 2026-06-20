package com.example.smartreview.data.repository

import com.example.smartreview.data.model.Course
import java.util.concurrent.ConcurrentHashMap

object CourseCache {
    private val store = ConcurrentHashMap<String, Course>()

    fun get(courseId: String): Course? = store[courseId]

    fun put(course: Course) { store[course.id] = course }

    fun clear() = store.clear()
}
