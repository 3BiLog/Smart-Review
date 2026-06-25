package com.example.smartreview.data.learning

import android.content.Context
import android.content.SharedPreferences
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

object StudyTimeManager {
    private const val TAG = "StudyTimeManager"
    private const val TICK_INTERVAL_MS = 10_000L
    private const val PREF_NAME = "study_time_session"
    private const val KEY_LAST_DATE = "last_date"
    private const val KEY_FLUSHED_MINUTES = "flushed_minutes"

    private var trackingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val userRepository: UserRepository = UserRepositoryProvider.default
    private lateinit var prefs: SharedPreferences

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

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        resetIfNewDay()
        // Load flushed minutes from last session
        flushedMinutes = prefs.getLong(KEY_FLUSHED_MINUTES, 0)
        _pendingMinutes.value = 0L
    }

    private fun resetIfNewDay() {
        val today = getTodayDateKey()
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        if (lastDate != today) {
            // New day: reset all session state
            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putLong(KEY_FLUSHED_MINUTES, 0)
                .apply()
            flushedMinutes = 0L
            foregroundAccumulatedMs = 0L
            segmentStartMs = 0L
            _pendingMinutes.value = 0L
            scope.launch {
                userRepository.resetDailyStudyTime()
            }
            android.util.Log.d(TAG, "New day detected – study time reset")
        }
    }

    private fun getTodayDateKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    fun startTracking(screenName: String, onGoalCompleted: ((Long) -> Unit)? = null) {
        if (isTracking && currentScreen == screenName) return

        if (isTracking) {
            runBlockingFlush()
        }

        resetIfNewDay()

        this.onGoalCompleted = onGoalCompleted
        currentScreen = screenName
        isTracking = true
        isAppInForeground = true
        foregroundAccumulatedMs = 0L
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
        resetIfNewDay()
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
        resetIfNewDay()
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
                prefs.edit().putLong(KEY_FLUSHED_MINUTES, flushedMinutes).apply()
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

    fun forceResetDaily() {
        prefs.edit()
            .putString(KEY_LAST_DATE, getTodayDateKey())
            .putLong(KEY_FLUSHED_MINUTES, 0)
            .apply()
        flushedMinutes = 0L
        foregroundAccumulatedMs = 0L
        segmentStartMs = 0L
        _pendingMinutes.value = 0L
        android.util.Log.d(TAG, "Force reset daily")
    }

    fun checkAndResetDaily() {
        resetIfNewDay()
    }
}