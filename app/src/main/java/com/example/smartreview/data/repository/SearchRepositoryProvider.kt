package com.example.smartreview.data.repository

import com.example.smartreview.data.content.CourseSearchMapper
import com.example.smartreview.data.model.SearchResult
import kotlinx.coroutines.flow.first

object SearchRepositoryProvider {

    val default: SearchRepository = object : SearchRepository {
        private val courseRepository: CourseRepository = CourseRepositoryProvider.default

        override suspend fun getAllResults(): List<SearchResult> {
            return CourseSearchMapper.fromCourses(courseRepository.getAllCourses().first())
        }
    }
}