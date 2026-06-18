package com.example.smartreview.data.learning

import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Tracks study time while the user is on learning screens.
 * Only counts time when the app is in the foreground.
 */
object StudyTimeManager {
    private const val TAG = "StudyTimeManager"
    private const val TICK_INTERVAL_MS = 10_000L

    private var trackingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val userRepository: UserRepository = UserRepositoryProvider.default

    /** Minutes in the current session not yet reflected in Firestore todayStudyTime. */
    private val _pendingMinutes = MutableStateFlow(0L)
    val totalStudyMinutes: StateFlow<Long> = _pendingMinutes.asStateFlow()

    private var isTracking = false
    private var isAppInForeground = true
    private var currentScreen: String? = null
    private var onGoalCompleted: ((Long) -> Unit)? = null

    private var foregroundAccumulatedMs = 0L
    private var segmentStartMs = 0L
    private var flushedMinutes = 0L
    private val persistMutex = Mutex()

    private val studyScreens = setOf(
        "LessonVideoPlayerScreen",
        "LessonScreen",
        "QuizScreen",
        "FlashcardScreen",
        "ReadingScreen",
        "LessonContentScreen",
        "PomodoroScreen",
    )

    fun startTracking(screenName: String, onGoalCompleted: ((Long) -> Unit)? = null) {
        if (isTracking && currentScreen == screenName) return

        if (isTracking) {
            runBlockingFlush()
        }

        this.onGoalCompleted = onGoalCompleted
        currentScreen = screenName
        isTracking = true
        isAppInForeground = true
        foregroundAccumulatedMs = 0L
        flushedMinutes = 0L
        segmentStartMs = System.currentTimeMillis()
        _pendingMinutes.value = 0L

        android.util.Log.d(TAG, "Started tracking on: $screenName")
        startTickLoop()
    }

    fun stopTracking() {
        if (!isTracking) return

        runBlockingFlush()

        isTracking = false
        trackingJob?.cancel()
        trackingJob = null
        currentScreen = null
        onGoalCompleted = null
        foregroundAccumulatedMs = 0L
        segmentStartMs = 0L
        flushedMinutes = 0L
        _pendingMinutes.value = 0L

        android.util.Log.d(TAG, "Stopped tracking")
    }

    fun isStudyScreen(screenName: String): Boolean {
        return studyScreens.any { screenName.contains(it) }
    }

    fun onAppPaused() {
        if (!isTracking) return
        isAppInForeground = false
        accumulateCurrentSegment()
        runBlockingFlush()
        trackingJob?.cancel()
        trackingJob = null
        android.util.Log.d(TAG, "App paused – tracking suspended")
    }

    fun onAppResumed() {
        if (!isTracking || currentScreen == null) return
        isAppInForeground = true
        segmentStartMs = System.currentTimeMillis()
        startTickLoop()
        android.util.Log.d(TAG, "App resumed – tracking resumed on: $currentScreen")
    }

    private fun accumulateCurrentSegment() {
        if (segmentStartMs > 0L) {
            foregroundAccumulatedMs += System.currentTimeMillis() - segmentStartMs
            segmentStartMs = 0L
        }
    }

    private fun totalForegroundMs(): Long {
        val segmentMs = if (isAppInForeground && segmentStartMs > 0L) {
            System.currentTimeMillis() - segmentStartMs
        } else {
            0L
        }
        return foregroundAccumulatedMs + segmentMs
    }

    private fun unsavedMinutes(): Long {
        val totalMinutes = totalForegroundMs() / 60_000L
        return (totalMinutes - flushedMinutes).coerceAtLeast(0)
    }

    private fun refreshPendingFlow() {
        _pendingMinutes.value = unsavedMinutes()
    }

    private fun startTickLoop() {
        trackingJob?.cancel()
        trackingJob = scope.launch {
            while (isActive && isTracking && isAppInForeground) {
                delay(TICK_INTERVAL_MS)
                if (isTracking && isAppInForeground) {
                    tick()
                }
            }
        }
    }

    private fun tick() {
        val pending = unsavedMinutes()
        refreshPendingFlow()
        if (pending > 0) {
            scope.launch { persistMinutes(pending) }
        }
    }

    private fun runBlockingFlush() {
        val pending = unsavedMinutes()
        if (pending > 0) {
            runBlocking(Dispatchers.IO) { persistMinutes(pending) }
        }
    }

    private suspend fun persistMinutes(minutes: Long) {
        persistMutex.withLock {
            val toSave = unsavedMinutes().coerceAtMost(minutes)
            if (toSave <= 0) return

            try {
                val before = userRepository.getCurrentUserProfile()
                val wasCompletedBefore = before?.isDailyGoalCompleted() ?: false

                val success = userRepository.addStudyTime(toSave)
                if (!success) return

                flushedMinutes += toSave
                refreshPendingFlow()

                android.util.Log.d(TAG, "Saved $toSave min (flushed=$flushedMinutes)")

                val profile = userRepository.getCurrentUserProfile()
                profile?.let {
                    val justCompleted = !wasCompletedBefore && it.isDailyGoalCompleted()
                    if (justCompleted && it.dailyGoalXP > 0) {
                        onGoalCompleted?.invoke(it.dailyGoalXP)
                        android.util.Log.d(TAG, "Daily goal completed! +${it.dailyGoalXP} XP")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error saving study time", e)
            }
        }
    }
}
