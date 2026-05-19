package com.example.smartreview.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.screens.coursedetail.courseDetailRoute
import com.example.smartreview.ui.screens.search.components.*
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.delay

const val SEARCH_ROUTE = "search"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    vm: SearchViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        delay(250)
        focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            SearchTopBar(
                query = state.searchQuery,
                onQueryChange = { vm.onQueryChange(it) },
                onClear = { vm.clearQuery() },
                onBack = { navController.popBackStack() },
                onFilterClick = { vm.openAdvancedSheet() },
                focusRequester = focusRequester,
                onSearch = { focusManager.clearFocus() },
            )
        },
        bottomBar = { SmartReviewBottomBar(navController) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchFilterChips(
                filters = state.filters,
                selectedFilter = state.selectedFilter,
                onSelect = { vm.onFilterSelect(it) },
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isSearching -> {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    state.searchQuery.isBlank() && state.selectedFilter == "Tất cả" -> {
                        SearchInitialContent(
                            suggestions = state.suggestions,
                            onSuggestion = { vm.onQueryChange(it) },
                        )
                    }

                    state.displayedResults.isEmpty() -> {
                        SearchEmptyState(query = state.searchQuery)
                    }

                    else -> {
                        SearchResultsContent(
                            results = state.displayedResults,
                            sortBy = state.sortBy,
                            isSortMenuOpen = state.isSortMenuOpen,
                            onOpenSort = { vm.openSortMenu() },
                            onCloseSort = { vm.closeSortMenu() },
                            onSortChange = { vm.onSortChange(it) },
                            onItemClick = { result ->
                                focusManager.clearFocus()
                                navController.navigate(courseDetailRoute(result.id))
                            },
                        )
                    }
                }
            }
        }
    }

    if (state.showAdvancedSheet) {
        ModalBottomSheet(
            onDismissRequest = { vm.closeAdvancedSheet() },
            sheetState = sheetState,
            containerColor = Surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(OnSurfaceVariant.copy(0.3f)),
                )
            },
        ) {
            SearchAdvancedFiltersSheet(
                state = state,
                onPriceChange = { vm.onPriceUpperChange(it) },
                onRatingChange = { vm.onMinRatingChange(it) },
                onApply = { vm.applyAdvancedFilters() },
                onReset = { vm.resetAdvancedFilters() },
            )
        }
    }
}
