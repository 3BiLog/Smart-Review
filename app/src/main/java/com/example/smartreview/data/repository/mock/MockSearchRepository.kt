package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.mock.MockSearchData
import com.example.smartreview.data.model.SearchResult
import com.example.smartreview.data.repository.SearchRepository

/**
 * Mock implementation backed by [MockSearchData].
 * Replace with remote/local sources when Retrofit/Room are introduced.
 */
class MockSearchRepository : SearchRepository {

    override fun getAllResults(): List<SearchResult> = MockSearchData.allResults
}
