package com.example.smartreview.data.repository

import com.example.smartreview.data.model.SearchResult

interface SearchRepository {

    suspend fun getAllResults(): List<SearchResult>
}