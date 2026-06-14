package com.example.smartreview.data.model

/**
 * Reading lesson models matching Firestore schema from Web Admin
 *
 * Firestore structure:
 * courses/{courseId}/modules/{moduleId}/lessons/{lessonId}
 *   type: "reading"
 *   content.data {
 *     markdown: "Nội dung bài đọc..."
 *   }
 */
data class ReadingLesson(
    val id: String = "",
    val title: String = "",
    val type: String = "reading",
    val duration: Long = 0,
    val isFree: Boolean = false,
    val xpReward: Long = 50,
    val order: Long = 0,
    val markdown: String = "",
    val courseId: String? = null,
    val moduleId: String? = null
)

data class ReadingProgress(
    val lessonId: String,
    val isCompleted: Boolean = false,
    val completedAt: Long = 0,
    val lastPosition: Int = 0  // Scroll position for markdown
)