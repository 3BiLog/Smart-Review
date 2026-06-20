package com.example.smartreview.data.model

data class Course(
    val id:              String,
    val title:           String,
    val imageUrl:        String,
    val difficulty:      String,
    val lessonCount:     Int,
    val progress:        Float,
    val xpReward:        Int,
    val price:           Long,
    val reviewCount:     Int,
    val durationHours:   Float,
    val instructorName:  String,
    val instructorTitle: String,
    val instructorAvatar:String,
    val description:     String,
    val category:        String,
    val isBestseller:    Boolean          = false,
    val modules:         List<CourseModule> = emptyList(),
    val rating: Float = 0f,
    val ratingCount: Long = 0,
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
    val durationLabel: String,
    val lessons:       List<LessonItem>,
    val isLocked:      Boolean,
)

enum class LessonType { VIDEO, READING, QUIZ, FLASHCARD, UNKNOWN }

data class LessonItem(
    val id:               String,
    val title:            String,
    val durationSeconds:  Int,
    val thumbnailUrl:     String,
    val isLocked:         Boolean,
    val isCurrentlyPlaying: Boolean = false,
    val videoUrl:         String = "",
    val xpReward:         Int = 0,
    val lessonType:       LessonType = LessonType.VIDEO,
    val quizId:            String? = null,
    val contentData:      Map<String, Any?>? = null,
) {
val formattedDuration: String
    get() {
        val m = durationSeconds / 60
        val s = durationSeconds % 60
        return "%d:%02d".format(m, s)
    }
}