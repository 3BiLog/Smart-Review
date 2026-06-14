package com.example.smartreview.ui.screens.reading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.model.ReadingLesson
import com.example.smartreview.data.repository.firestore.FirestoreReadingRepository
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ReadingUiState(
    val reading: ReadingLesson? = null,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val isCompleting: Boolean = false,
    val showSuccess: Boolean = false,
    val error: String? = null
)

class ReadingViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val readingRepository: FirestoreReadingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()

    private val lessonId: String = savedStateHandle["lessonId"] ?: ""

    init {
        android.util.Log.d("ReadingViewModel", "Initializing with lessonId=$lessonId")
        loadReading()
    }

    private fun loadReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val reading = readingRepository.getReadingLesson(lessonId)
                android.util.Log.d("ReadingViewModel", "Reading loaded: ${reading != null}")
                _uiState.update {
                    it.copy(
                        reading = reading,
                        isLoading = false,
                        error = if (reading == null) "Không tìm thấy bài đọc" else null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReadingViewModel", "Error loading reading", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi tải nội dung: ${e.message}"
                    )
                }
            }
        }
    }

    fun completeReading() {
        viewModelScope.launch {
            val reading = _uiState.value.reading ?: return@launch
            val userId = userRepository.getCurrentUserProfile()?.uid ?: run {
                _uiState.update { it.copy(error = "Vui lòng đăng nhập để nhận XP") }
                return@launch
            }

            _uiState.update { it.copy(isCompleting = true, error = null) }

            try {
                // Add XP directly via Firestore
                val firestore = FirebaseFirestore.getInstance()

                // 1. Update user's totalXP
                val userRef = firestore.collection("users").document(userId)
                userRef.update("totalXP", FieldValue.increment(reading.xpReward)).await()

                // 2. Add XP log
                val xpLog = mapOf(
                    "userId" to userId,
                    "amount" to reading.xpReward,
                    "activityType" to "reading_complete",
                    "reason" to "Completed reading: ${reading.title}",
                    "timestamp" to Timestamp.now()
                )
                firestore.collection("xp_logs").add(xpLog).await()

                android.util.Log.d("ReadingViewModel", "XP added: +${reading.xpReward}")

                _uiState.update {
                    it.copy(
                        isCompleted = true,
                        isCompleting = false,
                        showSuccess = true
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReadingViewModel", "Error adding XP", e)
                _uiState.update {
                    it.copy(
                        isCompleting = false,
                        error = "Không thể cộng XP: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(lessonId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val firestore = FirebaseFirestore.getInstance()
                    val readingRepository = FirestoreReadingRepository(firestore)
                    return ReadingViewModel(
                        SavedStateHandle(mapOf("lessonId" to lessonId)),
                        readingRepository,
                        UserRepositoryProvider.default
                    ) as T
                }
            }
    }
}