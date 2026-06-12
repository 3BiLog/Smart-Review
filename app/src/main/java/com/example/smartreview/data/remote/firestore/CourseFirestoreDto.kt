package com.example.smartreview.data.remote.firestore

/**
 * Firestore document shapes for courses/{courseId}.
 *
 * Field names match DA3-master production schema (embedded modules[] / lessons[]).
 * Document ID is the course ID and is not duplicated in these DTOs.
 */
object CourseFirestorePaths {
    const val COURSES = "courses"

    object Fields {
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val PRICE = "price"
        const val CATEGORY = "category"
        const val LEVEL = "level"
        const val STATUS = "status"
        const val THUMBNAIL_URL = "thumbnailUrl"
        const val TOTAL_DURATION_HOURS = "totalDurationHours"
        const val RATING = "rating"
        const val RATING_COUNT = "ratingCount"
        const val MODULES = "modules"
        const val FEATURED_ORDER = "featuredOrder"
    }

    object ModuleFields {
        const val ID = "id"
        const val TITLE = "title"
        const val DURATION = "duration"
        const val ORDER = "order"
        const val LESSONS = "lessons"
    }

    object LessonFields {
        const val ID = "id"
        const val TITLE = "title"
        const val TYPE = "type"
        const val DURATION = "duration"
        const val XP_REWARD = "xpReward"
        const val IS_FREE = "isFree"
        const val ORDER = "order"
        const val VIDEO_URL = "videoUrl"
        const val QUIZ_ID = "quizId"
        const val CONTENT = "content"
    }

    object ContentFields {
        const val DATA = "data"
        const val MARKDOWN = "markdown"
        const val TEXT = "text"
        const val QUESTIONS = "questions"
        const val CARDS = "cards"
        const val OPTIONS = "options"
        const val TITLE = "title"
        const val PROMPT = "prompt"
        const val ID = "id"
        const val CORRECT = "correct"
        const val EXPLANATION = "explanation"
        const val LABEL = "label"
        const val ANSWER = "answer"
        const val KEYWORD = "keyword"
    }
}

data class CourseDocument(
    val title: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val category: String? = null,
    val level: String? = null,
    val status: String? = null,
    val thumbnailUrl: String? = null,
    val totalDurationHours: Double? = null,
    val rating: Double? = null,
    val ratingCount: Long? = null,
    val featuredOrder: Long? = null,
    val modules: List<CourseModuleDocument> = emptyList(),
)

data class CourseModuleDocument(
    val id: String? = null,
    val title: String? = null,
    val durationMinutes: Long? = null,
    val order: Long? = null,
    val lessons: List<CourseLessonDocument> = emptyList(),
)

data class CourseLessonDocument(
    val id: String? = null,
    val title: String? = null,
    val type: String? = null,
    val durationMinutes: Long? = null,
    val xpReward: Long? = null,
    val isFree: Boolean? = null,
    val order: Long? = null,
    val videoUrl: String? = null,
    val quizId: String? = null,
    val contentData: Map<String, Any?>? = null,
)
