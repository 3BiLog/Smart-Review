package com.example.smartreview.ui.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.learning.LessonContentBlocks
import com.example.smartreview.data.lesson.LessonProgressStore
import com.example.smartreview.data.lesson.LessonSessionStore
import com.example.smartreview.data.model.LessonBlock
import com.example.smartreview.data.model.LessonCompletionResult
import com.example.smartreview.data.model.LessonContent
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.repository.LessonRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LessonUiState(
    val lesson: LessonContent? = null,
    val contentBlocks: List<LessonBlock> = emptyList(),
    val linkedQuizId: String? = null,
    val viewedBlockCount: Int = 0,
    val isContentReady: Boolean = false,
    val isLoading: Boolean = true,
    val alreadyCompleted: Boolean = false,
    val isResuming: Boolean = false,
)

class LessonViewModel(
    private val lessonId: String,
    private val lessonRepository: LessonRepository = LessonRepositoryProvider.default,
) : ViewModel() {

    private var sessionId: String = UUID.randomUUID().toString()
    private var sessionStartedAt: Long = System.currentTimeMillis()
    private val viewedBlockIds = mutableSetOf<String>()
    private val progressService = LearningProgressServiceProvider.default

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadLesson() }
    }

    fun markContentViewed() {
        val blocks = _uiState.value.contentBlocks
        if (blocks.isEmpty()) return
        viewedBlockIds.addAll(blocks.map { it.id })
        _uiState.update {
            it.copy(
                viewedBlockCount = blocks.size,
                isContentReady = true,
            )
        }
        persistContentProgress()
    }

    fun completeLesson(): String? {
        val state = _uiState.value
        val lesson = state.lesson ?: return null
        if (!state.isContentReady) return null

        val result = LessonCompletionResult(
            sessionId = sessionId,
            lessonId = lesson.id,
            courseId = lesson.courseId,
            lessonTitle = lesson.title,
            totalBlocks = state.contentBlocks.size.coerceAtLeast(1),
            viewedBlocks = state.viewedBlockCount.coerceAtLeast(1),
            durationMs = System.currentTimeMillis() - sessionStartedAt,
        )
        LessonSessionStore.put(result)
        viewModelScope.launch {
            LessonProgressStore.markCompleted(lesson.id)
        }
        return sessionId
    }

    private suspend fun loadLesson() {
        val lesson = lessonRepository.getLesson(lessonId)
        if (lesson == null) {
            _uiState.value = LessonUiState(isLoading = false)
            return
        }

        val contentBlocks = LessonContentBlocks.contentBlocks(lesson)
        val linkedQuizId = LessonContentBlocks.linkedQuizId(lesson)
        var isResuming = false
        val snapshot = progressService.lessonSnapshotForLesson(lessonId)
        if (snapshot != null) {
            isResuming = true
            sessionId = snapshot.sessionId
            sessionStartedAt = snapshot.sessionStartedAt
            viewedBlockIds.addAll(snapshot.viewedBlockIds.intersect(contentBlocks.map { it.id }.toSet()))
        }

        val alreadyCompleted = progressService.isLessonCompleted(lesson.id)
        val viewedCount = contentBlocks.count { it.id in viewedBlockIds }

        _uiState.value = LessonUiState(
            lesson = lesson,
            contentBlocks = contentBlocks,
            linkedQuizId = linkedQuizId,
            viewedBlockCount = viewedCount,
            isContentReady = contentBlocks.isNotEmpty() && viewedCount >= contentBlocks.size,
            isLoading = false,
            alreadyCompleted = alreadyCompleted,
            isResuming = isResuming,
        )
        if (!alreadyCompleted) {
            persistContentProgress()
        }
    }

    private fun persistContentProgress() {
        val state = _uiState.value
        val lesson = state.lesson ?: return
        if (state.alreadyCompleted) return
        viewModelScope.launch {
            progressService.saveLessonSnapshot(
                LessonProgressSnapshot(
                    lessonId = lesson.id,
                    sessionId = sessionId,
                    sessionStartedAt = sessionStartedAt,
                    currentBlockIndex = 0,
                    viewedBlockIds = viewedBlockIds.toSet(),
                ),
            )
        }
    }

    companion object {
        fun provideFactory(lessonId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LessonViewModel(lessonId) as T
            }
    }
}
