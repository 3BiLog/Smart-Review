package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.mock.MockSearchRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 * Swap [default] in tests or when wiring a real data source.
 */
object SearchRepositoryProvider {
    val default: SearchRepository = MockSearchRepository()
}
