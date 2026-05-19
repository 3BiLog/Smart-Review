package com.example.smartreview.ui.screens.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class TimerStatus { IDLE, WORKING, BREAK, PAUSED }

data class PomodoroUiState(
    val workMinutes:      Int         = 25,
    val breakMinutes:     Int         = 5,
    val totalSeconds:     Int         = 25 * 60,
    val remainingSeconds: Int         = 25 * 60,
    val status:           TimerStatus = TimerStatus.IDLE,
    val cycle:            Int         = 1,          // current cycle (1–4)
    val deepFocusEnabled: Boolean     = false,
) {
    val progress: Float get() = remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1)
    val displayTime: String get() {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        return "%02d:%02d".format(m, s)
    }
    val isRunning get() = status == TimerStatus.WORKING || status == TimerStatus.BREAK
}

class PomodoroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun togglePlayPause() {
        when (_uiState.value.status) {
            TimerStatus.IDLE, TimerStatus.PAUSED -> startTimer()
            TimerStatus.WORKING, TimerStatus.BREAK -> pauseTimer()
        }
    }

    private fun startTimer() {
        val status = if (_uiState.value.status == TimerStatus.BREAK) TimerStatus.BREAK else TimerStatus.WORKING
        _uiState.update { it.copy(status = status) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.isRunning) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            if (_uiState.value.remainingSeconds == 0) onTimerFinished()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(status = TimerStatus.PAUSED) }
    }

    private fun onTimerFinished() {
        if (_uiState.value.status == TimerStatus.WORKING) {
            // Switch to break
            val breakSec = _uiState.value.breakMinutes * 60
            _uiState.update { it.copy(status = TimerStatus.BREAK, totalSeconds = breakSec, remainingSeconds = breakSec) }
            startTimer()
        } else {
            // Break over → next cycle
            val newCycle = (_uiState.value.cycle % 4) + 1
            val workSec  = _uiState.value.workMinutes * 60
            _uiState.update { it.copy(status = TimerStatus.IDLE, cycle = newCycle, totalSeconds = workSec, remainingSeconds = workSec) }
        }
    }

    fun reset() {
        timerJob?.cancel()
        val workSec = _uiState.value.workMinutes * 60
        _uiState.update { it.copy(status = TimerStatus.IDLE, totalSeconds = workSec, remainingSeconds = workSec) }
    }

    fun skipToNext() = onTimerFinished()

    fun incrementWork() = updateWork(_uiState.value.workMinutes + 5)
    fun decrementWork() = updateWork((_uiState.value.workMinutes - 5).coerceAtLeast(5))
    fun incrementBreak() = updateBreak(_uiState.value.breakMinutes + 1)
    fun decrementBreak() = updateBreak((_uiState.value.breakMinutes - 1).coerceAtLeast(1))

    private fun updateWork(minutes: Int) {
        val sec = minutes * 60
        _uiState.update { it.copy(workMinutes = minutes, totalSeconds = sec, remainingSeconds = sec, status = TimerStatus.IDLE) }
        timerJob?.cancel()
    }

    private fun updateBreak(minutes: Int) {
        _uiState.update { it.copy(breakMinutes = minutes) }
    }

    fun toggleDeepFocus() = _uiState.update { it.copy(deepFocusEnabled = !it.deepFocusEnabled) }

    override fun onCleared() { timerJob?.cancel() }
}