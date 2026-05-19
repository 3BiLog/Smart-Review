package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.mock.MockCourseRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 * Swap [default] in tests or when wiring a real data source.
 */
object CourseRepositoryProvider {
    val default: CourseRepository = MockCourseRepository()
}
