package com.example.smartreview.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.model.SearchResult
import com.example.smartreview.data.model.SortOption
import com.example.smartreview.data.repository.SearchRepository
import com.example.smartreview.data.repository.SearchRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val filters: List<String> = listOf(
        "Tất cả", "Lập trình", "Sản phẩm",
    ),
    val selectedFilter: String = "Tất cả",
    val sortBy: SortOption = SortOption.POPULAR,
    val isSortMenuOpen: Boolean = false,
    val allResults: List<SearchResult> = emptyList(),
    val displayedResults: List<SearchResult> = emptyList(),
    val showAdvancedSheet: Boolean = false,
    val filterMaxPrice: Long = 2_000_000L,
    val filterPriceUpper: Long = 2_000_000L,
    val filterMinRating: Float = 0f,
    val suggestions: List<String> = listOf(
        "React", "Compose", "Android", "Sản phẩm", "ViewModel",
    ),
)

class SearchViewModel(
    private val searchRepository: SearchRepository = SearchRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init { loadCatalog() }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = query.isNotBlank()) }
        debounceSearch(query)
    }

    fun clearQuery() = onQueryChange("")

    fun onFilterSelect(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        debounceSearch(_uiState.value.searchQuery)
    }

    fun openSortMenu() = _uiState.update { it.copy(isSortMenuOpen = true) }
    fun closeSortMenu() = _uiState.update { it.copy(isSortMenuOpen = false) }

    fun onSortChange(option: SortOption) {
        _uiState.update { it.copy(sortBy = option, isSortMenuOpen = false) }
        applyCurrentFilters()
    }

    fun openAdvancedSheet() = _uiState.update { it.copy(showAdvancedSheet = true) }
    fun closeAdvancedSheet() = _uiState.update { it.copy(showAdvancedSheet = false) }

    fun onPriceUpperChange(upper: Long) = _uiState.update { it.copy(filterPriceUpper = upper) }
    fun onMinRatingChange(rating: Float) = _uiState.update { it.copy(filterMinRating = rating) }

    fun applyAdvancedFilters() {
        closeAdvancedSheet()
        applyCurrentFilters()
    }

    fun resetAdvancedFilters() {
        _uiState.update {
            it.copy(
                filterPriceUpper = it.filterMaxPrice,
                filterMinRating = 0f,
            )
        }
        applyCurrentFilters()
    }

    private fun debounceSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(280)
            applyCurrentFilters()
        }
    }

    private fun applyCurrentFilters() {
        val state = _uiState.value
        val filtered = state.allResults.filter { r ->
            val matchesQuery = state.searchQuery.isBlank() ||
                r.title.contains(state.searchQuery, ignoreCase = true) ||
                r.instructorName.contains(state.searchQuery, ignoreCase = true) ||
                r.category.contains(state.searchQuery, ignoreCase = true)
            val matchesCategory = state.selectedFilter == "Tất cả" ||
                r.category == state.selectedFilter
            val matchesPrice = r.price == 0L || r.price <= state.filterPriceUpper
            val matchesRating = r.rating >= state.filterMinRating

            matchesQuery && matchesCategory && matchesPrice && matchesRating
        }
        val sorted = applySorting(filtered, state.sortBy)
        _uiState.update { it.copy(displayedResults = sorted, isSearching = false) }
    }

    private fun applySorting(list: List<SearchResult>, sort: SortOption) =
        when (sort) {
            SortOption.POPULAR -> list
            SortOption.NEWEST -> list.reversed()
            SortOption.PRICE_LOW -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH -> list.sortedByDescending { it.price }
            SortOption.RATING -> list.sortedByDescending { it.rating }
        }

    private fun loadCatalog() {
        val catalog = searchRepository.getAllResults()
        val maxPrice = catalog.maxOfOrNull { it.price }?.coerceAtLeast(0L) ?: 0L
        _uiState.update {
            it.copy(
                allResults = catalog,
                displayedResults = catalog,
                filterMaxPrice = maxPrice.coerceAtLeast(1_000_000L),
                filterPriceUpper = maxPrice.coerceAtLeast(1_000_000L),
            )
        }
    }
}
