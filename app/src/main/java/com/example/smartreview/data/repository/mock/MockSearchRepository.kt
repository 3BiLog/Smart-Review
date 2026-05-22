package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.content.CourseSearchMapper
import com.example.smartreview.data.model.SearchResult
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.SearchRepository

/**
 * Search results derived from [CourseRepository] only (no separate fake catalog).
 */
class MockSearchRepository(
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
) : SearchRepository {

    override fun getAllResults(): List<SearchResult> =
        CourseSearchMapper.fromCourses(courseRepository.getCourses())
}
