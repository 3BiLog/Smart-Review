package com.example.smartreview.ui.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.learning.LearningProgressServiceProvider
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
    val currentBlockIndex: Int = 0,
    val viewedBlockCount: Int = 0,
    val remainingBlockCount: Int = 0,
    val isLessonComplete: Boolean = false,
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

    val totalBlocks: Int get() = _uiState.value.lesson?.blocks?.size ?: 0

    val currentBlock: LessonBlock?
        get() = _uiState.value.lesson?.blocks?.getOrNull(_uiState.value.currentBlockIndex)

    fun markCurrentBlockViewed() {
        val block = currentBlock ?: return
        viewedBlockIds.add(block.id)
        publishProgress()
    }

    fun nextBlock() {
        markCurrentBlockViewed()
        val lesson = _uiState.value.lesson ?: return
        if (lesson.blocks.isEmpty()) return
        val next = (_uiState.value.currentBlockIndex + 1).coerceAtMost(lesson.blocks.lastIndex)
        _uiState.update { it.copy(currentBlockIndex = next) }
        checkCompletion()
    }

    fun previousBlock() {
        val prev = (_uiState.value.currentBlockIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentBlockIndex = prev) }
        persistProgress()
    }

    fun completeLesson(): String? {
        val state = _uiState.value
        val lesson = state.lesson ?: return null
        if (!state.isLessonComplete) return null

        val result = LessonCompletionResult(
            sessionId = sessionId,
            lessonId = lesson.id,
            courseId = lesson.courseId,
            lessonTitle = lesson.title,
            totalBlocks = lesson.blocks.size,
            viewedBlocks = state.viewedBlockCount,
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

        viewedBlockIds.clear()
        var isResuming = false
        val snapshot = progressService.lessonSnapshotForLesson(lessonId)
        if (snapshot != null) {
            isResuming = true
            sessionId = snapshot.sessionId
            sessionStartedAt = snapshot.sessionStartedAt
            viewedBlockIds.addAll(snapshot.viewedBlockIds)
        } else if (lesson.blocks.isNotEmpty()) {
            viewedBlockIds.add(lesson.blocks.first().id)
        }

        val alreadyCompleted = progressService.isLessonCompleted(lesson.id)

        _uiState.value = LessonUiState(
            lesson = lesson,
            currentBlockIndex = snapshot?.currentBlockIndex ?: 0,
            remainingBlockCount = lesson.blocks.size,
            viewedBlockCount = viewedBlockIds.size.coerceAtMost(lesson.blocks.size),
            isLoading = false,
            alreadyCompleted = alreadyCompleted,
            isResuming = isResuming,
        )
        checkCompletion()
        persistProgress()
    }

    private fun checkCompletion() {
        val lesson = _uiState.value.lesson ?: return
        val allViewed = lesson.blocks.isNotEmpty() &&
            lesson.blocks.all { it.id in viewedBlockIds }
        if (allViewed) {
            _uiState.update { it.copy(isLessonComplete = true) }
            viewModelScope.launch { progressService.clearLessonInProgress() }
        } else {
            persistProgress()
        }
    }

    private fun publishProgress() {
        val lesson = _uiState.value.lesson ?: return
        val viewed = lesson.blocks.count { it.id in viewedBlockIds }
        _uiState.update {
            it.copy(
                viewedBlockCount = viewed,
                remainingBlockCount = (lesson.blocks.size - viewed).coerceAtLeast(0),
            )
        }
        persistProgress()
    }

    private fun persistProgress() {
        val state = _uiState.value
        val lesson = state.lesson ?: return
        if (state.isLessonComplete) return
        viewModelScope.launch {
            progressService.saveLessonSnapshot(
                LessonProgressSnapshot(
                    lessonId = lesson.id,
                    sessionId = sessionId,
                    sessionStartedAt = sessionStartedAt,
                    currentBlockIndex = state.currentBlockIndex,
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
