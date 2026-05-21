package com.example.smartreview.data.model

/**
 * Local-first lesson content (blocks). Not synced to Firestore yet.
 */
enum class LessonBlockType {
    HEADING,
    TEXT,
    IMAGE,
    TIP,
    /** Placeholder for future quiz integration — links via [LessonBlock.quizStubId]. */
    QUIZ_STUB,
}

data class LessonBlock(
    val id: String,
    val type: LessonBlockType,
    val title: String? = null,
    val body: String = "",
    val imageUrl: String? = null,
    val quizStubId: String? = null,
)

data class LessonContent(
    val id: String,
    val courseId: String,
    val title: String,
    val subtitle: String,
    val estimatedMinutes: Int,
    val blocks: List<LessonBlock>,
    val xpReward: Int = 50,
)

data class LessonCompletionResult(
    val sessionId: String,
    val lessonId: String,
    val courseId: String,
    val lessonTitle: String,
    val totalBlocks: Int,
    val viewedBlocks: Int,
    val durationMs: Long,
    val completedAt: Long = System.currentTimeMillis(),
) {
    val progress: Float
        get() = if (totalBlocks <= 0) 0f else viewedBlocks.toFloat() / totalBlocks

    fun formattedStudyTime(): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
