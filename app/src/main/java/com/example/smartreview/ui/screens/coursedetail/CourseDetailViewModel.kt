package com.example.smartreview.ui.screens.coursedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CourseDetailUiState(
    val course:           Course?      = null,
    val expandedModuleIds: Set<String> = setOf(),   // first module expanded by default
    val isBookmarked:     Boolean      = false,
    val isLoading:        Boolean      = true,
)

class CourseDetailViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init { loadCourse() }

    private fun loadCourse() {
        val course = courseRepository.getCourseById(courseId)

        // Open first unlocked module by default
        val firstModuleId = course.modules.firstOrNull { !it.isLocked }?.id
        _uiState.update {
            it.copy(
                course           = course,
                expandedModuleIds = if (firstModuleId != null) setOf(firstModuleId) else emptySet(),
                isLoading        = false,
            )
        }
    }

    fun toggleModule(moduleId: String) {
        _uiState.update { state ->
            val ids = state.expandedModuleIds
            state.copy(expandedModuleIds = if (moduleId in ids) ids - moduleId else ids + moduleId)
        }
    }

    fun toggleBookmark() = _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }

    // ─── Factory (no Hilt needed) ─────────────────────────────────────────
    companion object {
        fun provideFactory(courseId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CourseDetailViewModel(courseId) as T
            }
    }
}