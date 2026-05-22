package com.example.smartreview.data.model

// ─── Core domain models ───────────────────────────────────────────────────────

data class Course(
    val id:              String,
    val title:           String,
    val imageUrl:        String,
    val difficulty:      String,     // "Beginner" | "Intermediate" | "Advanced"
    val lessonCount:     Int,
    val progress:        Float,       // 0f–1f
    val xpReward:        Int,
    val price:           Long,        // VND, 0 = free
    val rating:          Float,
    val reviewCount:     Int,
    val durationHours:   Float,
    val instructorName:  String,
    val instructorTitle: String,
    val instructorAvatar:String,
    val description:     String,
    val category:        String,
    val isBestseller:    Boolean          = false,
    val modules:         List<CourseModule> = emptyList(),
) {
    val formattedPrice: String
        get() = if (price == 0L) "Miễn phí"
        else "%,dđ".format(price)

    val formattedDuration: String
        get() = if (durationHours == durationHours.toLong().toFloat())
            "${durationHours.toInt()}h"
        else "${durationHours}h"
}

data class CourseModule(
    val id:            String,
    val title:         String,
    val lessonCount:   Int,
    val durationLabel: String,            // "3 Lessons • 45m"
    val lessons:       List<LessonItem>,
    val isLocked:      Boolean,
)

data class LessonItem(
    val id:               String,
    val title:            String,
    val durationSeconds:  Int,
    val thumbnailUrl:     String,
    val isLocked:         Boolean,
    val isCurrentlyPlaying: Boolean = false,
    /** YouTube URL for [LessonVideoPlayerScreen]; may be enriched from [LessonContent]. */
    val videoUrl:         String = "",
) {
    val formattedDuration: String
        get() {
            val m = durationSeconds / 60
            val s = durationSeconds % 60
            return "%d:%02d".format(m, s)
        }
}