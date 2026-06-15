package com.example.smartreview.data.repository

import com.example.smartreview.data.model.SearchResult

/**
 * Data access contract for the Search feature.
 * ViewModels depend on this interface — results come from [com.example.smartreview.data.repository.CourseRepository].
 */
interface SearchRepository {

    suspend fun getAllResults(): List<SearchResult>  // ✅ Thêm suspend
}