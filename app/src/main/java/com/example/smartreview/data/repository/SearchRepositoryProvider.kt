package com.example.smartreview.data.repository

import com.example.smartreview.data.content.CourseSearchMapper
import com.example.smartreview.data.model.SearchResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 * Swap [default] in tests or when wiring a real data source.
 */
object SearchRepositoryProvider {

    val default: SearchRepository = object : SearchRepository {
        private val courseRepository: CourseRepository = CourseRepositoryProvider.default

        override fun getAllResults(): List<SearchResult> = runBlocking {
            CourseSearchMapper.fromCourses(courseRepository.getAllCourses().first())
        }
    }
}
