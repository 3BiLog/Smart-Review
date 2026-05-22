package com.example.smartreview.data.content

import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.SearchResult

object CourseSearchMapper {

    fun fromCourses(courses: List<Course>): List<SearchResult> =
        courses.map { course ->
            SearchResult(
                id = course.id,
                title = course.title,
                instructorName = course.instructorName,
                thumbnailUrl = course.imageUrl,
                price = course.price,
                durationLabel = course.formattedDuration,
                category = course.category,
                rating = course.rating,
                level = course.difficulty,
            )
        }
}
