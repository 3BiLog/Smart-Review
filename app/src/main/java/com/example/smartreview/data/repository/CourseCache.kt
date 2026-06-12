package com.example.smartreview.data.repository

import com.example.smartreview.data.model.Course
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory cache for Course objects with TTL (no TTL implemented yet).
 * Used to reduce repeated Firestore reads for course documents.
 */
object CourseCache {
    private val store = ConcurrentHashMap<String, Course>()

    fun get(courseId: String): Course? = store[courseId]

    fun put(course: Course) { store[course.id] = course }

    fun clear() = store.clear()
}
