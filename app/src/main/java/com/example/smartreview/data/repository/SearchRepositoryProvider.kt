package com.example.smartreview.data.repository

import com.example.smartreview.data.content.CourseSearchMapper
import com.example.smartreview.data.model.SearchResult
import kotlinx.coroutines.flow.first

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 * Swap [default] in tests or when wiring a real data source.
 */
object SearchRepositoryProvider {

    val default: SearchRepository = object : SearchRepository {
        private val courseRepository: CourseRepository = CourseRepositoryProvider.default

        // ✅ Đã sửa: Bỏ runBlocking, dùng suspend
        override suspend fun getAllResults(): List<SearchResult> {
            return CourseSearchMapper.fromCourses(courseRepository.getAllCourses().first())
        }
    }
}