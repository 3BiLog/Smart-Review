package com.example.smartreview.data.repository

import com.example.smartreview.data.model.SearchResult

/**
 * Data access contract for the Search feature.
 * ViewModels depend on this interface — not on [com.example.smartreview.data.mock.MockSearchData].
 */
interface SearchRepository {

    fun getAllResults(): List<SearchResult>
}
