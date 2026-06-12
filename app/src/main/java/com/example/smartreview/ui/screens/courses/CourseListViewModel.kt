package com.example.smartreview.ui.screens.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseListUiState(
    val allCourses:      List<Course> = emptyList(),
    val filteredCourses: List<Course> = emptyList(),
    val selectedFilter:  String       = "Tất cả",
    val searchQuery:     String       = "",
    val filters:         List<String> = listOf("Tất cả", "Lập trình", "Toán", "Ngoại ngữ", "Kỹ năng mềm"),
)

class CourseListViewModel(
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseListUiState())
    val uiState: StateFlow<CourseListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val courses = courseRepository.getAllCourses().first()
            _uiState.update { it.copy(allCourses = courses, filteredCourses = courses) }
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allCourses, filter, state.searchQuery)
            state.copy(selectedFilter = filter, filteredCourses = filtered)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allCourses, state.selectedFilter, query)
            state.copy(searchQuery = query, filteredCourses = filtered)
        }
    }

    private fun applyFilters(courses: List<Course>, filter: String, query: String): List<Course> =
        courses.filter { course ->
            val matchesCategory = filter == "Tất cả" || course.category == filter
            val matchesQuery    = query.isBlank() ||
                    course.title.contains(query, ignoreCase = true) ||
                    course.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
}