package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.data.video.YouTubeVideoUrl

object  CourseFirestoreMapper {

    fun toCourse(documentId: String, data: Map<String, Any>?): Course? {
        if (data == null) return null
        val dto = mapToCourseDocument(data)
        val title = dto.title?.takeIf { it.isNotBlank() } ?: return null
        val modules = mapModules(dto.modules, dto.thumbnailUrl.orEmpty())
        val lessonCount = modules.sumOf { it.lessons.size }
        val totalXp = modules.sumOf { module -> module.lessons.sumOf { it.xpReward } }

        return Course(
            id = documentId,
            title = title,
            imageUrl = dto.thumbnailUrl.orEmpty(),
            difficulty = mapLevelToDifficulty(dto.level),
            lessonCount = lessonCount,
            progress = 0f,
            xpReward = totalXp,
            price = dto.price ?: 0L,
            reviewCount = dto.ratingCount?.toInt() ?: 0,
            durationHours = dto.totalDurationHours?.toFloat() ?: 0f,
            instructorName = "",
            instructorTitle = "",
            instructorAvatar = "",
            description = dto.description.orEmpty(),
            category = dto.category.orEmpty(),
            isBestseller = (dto.featuredOrder ?: 0L) > 0L,
            modules = modules,
            rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
            ratingCount = (data["ratingCount"] as? Number)?.toLong() ?: 0,
        )
    }

    private fun mapModules(
        moduleDocuments: List<CourseModuleDocument>,
        courseThumbnailUrl: String,
    ): List<CourseModule> {
        return moduleDocuments
            .sortedBy { it.order ?: Long.MAX_VALUE }
            .mapIndexed { index, moduleDoc ->
                val lessons = mapLessons(moduleDoc.lessons, courseThumbnailUrl)
                val moduleMinutes = moduleDoc.durationMinutes
                    ?: lessons.sumOf { it.durationSeconds / 60 }.toLong()
                CourseModule(
                    id = moduleDoc.id?.takeIf { it.isNotBlank() } ?: "module_$index",
                    title = moduleDoc.title.orEmpty().ifBlank { "Module ${index + 1}" },
                    lessonCount = lessons.size,
                    durationLabel = formatModuleDurationLabel(lessons.size, moduleMinutes),
                    lessons = lessons,
                    isLocked = false,
                )
            }
    }

    private fun mapLessons(
        lessonDocuments: List<CourseLessonDocument>,
        courseThumbnailUrl: String,
    ): List<LessonItem> {
        return lessonDocuments
            .sortedBy { it.order ?: Long.MAX_VALUE }
            .map { lessonDoc ->
                val durationMinutes = lessonDoc.durationMinutes ?: 0L
                val videoUrl = lessonDoc.videoUrl.orEmpty()
                val thumbnail = resolveLessonThumbnail(videoUrl, courseThumbnailUrl, lessonDoc.id)

                val rawType = lessonDoc.type
                val lowerType = rawType?.lowercase()
                android.util.Log.d("CourseFirestoreMapper", "Lesson: id=${lessonDoc.id}, title=${lessonDoc.title}, rawType=$rawType, lowerType=$lowerType")

                val lessonType = when (lowerType) {
                    "video" -> {
                        android.util.Log.d("CourseFirestoreMapper", "  -> Mapped to VIDEO")
                        LessonType.VIDEO
                    }
                    "reading", "read", "text" -> {
                        android.util.Log.d("CourseFirestoreMapper", "  -> Mapped to READING")
                        LessonType.READING
                    }
                    "quiz" -> {
                        android.util.Log.d("CourseFirestoreMapper", "  -> Mapped to QUIZ")
                        LessonType.QUIZ
                    }
                    "flashcard" -> {
                        android.util.Log.d("CourseFirestoreMapper", "  -> Mapped to FLASHCARD")
                        LessonType.FLASHCARD
                    }
                    else -> {
                        android.util.Log.d("CourseFirestoreMapper", "  -> Unknown type '$rawType', defaulting to VIDEO")
                        LessonType.VIDEO
                    }
                }

                LessonItem(
                    id = lessonDoc.id.orEmpty(),
                    title = lessonDoc.title.orEmpty(),
                    durationSeconds = (durationMinutes * 60L).toInt().coerceAtLeast(0),
                    thumbnailUrl = thumbnail,
                    isLocked = false,
                    videoUrl = videoUrl,
                    xpReward = lessonDoc.xpReward?.toInt() ?: 0,
                    lessonType = lessonType,
                    quizId = lessonDoc.quizId,
                    contentData = lessonDoc.contentData,
                )
            }
            .filter { it.id.isNotBlank() }
    }

    private fun resolveLessonThumbnail(
        videoUrl: String,
        courseThumbnailUrl: String,
        lessonId: String?,
    ): String {
        val videoId = YouTubeVideoUrl.extractVideoId(videoUrl)
        if (videoId != null) return YouTubeVideoUrl.thumbnailUrl(videoId)
        if (courseThumbnailUrl.isNotBlank()) return courseThumbnailUrl
        val seed = lessonId?.takeIf { it.isNotBlank() } ?: "lesson"
        return "https://picsum.photos/seed/$seed/320/180"
    }

    private fun formatModuleDurationLabel(lessonCount: Int, totalMinutes: Long): String {
        val minutes = totalMinutes.coerceAtLeast(1L)
        return "$lessonCount bài · $minutes phút"
    }

    private fun mapLevelToDifficulty(level: String?): String =
        when (level?.lowercase()) {
            "beginner" -> "Beginner"
            "intermediate" -> "Intermediate"
            "advanced" -> "Advanced"
            "all_levels" -> "All Levels"
            null, "" -> "Beginner"
            else -> level.replaceFirstChar { it.uppercase() }
        }

    private fun mapToCourseDocument(data: Map<String, Any?>): CourseDocument =
        CourseDocument(
            title = stringField(data, CourseFirestorePaths.Fields.TITLE),
            description = stringField(data, CourseFirestorePaths.Fields.DESCRIPTION),
            price = numberField(data, CourseFirestorePaths.Fields.PRICE),
            category = stringField(data, CourseFirestorePaths.Fields.CATEGORY),
            level = stringField(data, CourseFirestorePaths.Fields.LEVEL),
            status = stringField(data, CourseFirestorePaths.Fields.STATUS),
            thumbnailUrl = stringField(data, CourseFirestorePaths.Fields.THUMBNAIL_URL),
            totalDurationHours = doubleField(data, CourseFirestorePaths.Fields.TOTAL_DURATION_HOURS),
            rating = doubleField(data, CourseFirestorePaths.Fields.RATING),
            ratingCount = numberField(data, CourseFirestorePaths.Fields.RATING_COUNT),
            featuredOrder = numberField(data, CourseFirestorePaths.Fields.FEATURED_ORDER),
            modules = listField(data, CourseFirestorePaths.Fields.MODULES) { mapToModuleDocument(it) },
        )

    private fun mapToModuleDocument(raw: Map<String, Any?>): CourseModuleDocument =
        CourseModuleDocument(
            id = stringField(raw, CourseFirestorePaths.ModuleFields.ID),
            title = stringField(raw, CourseFirestorePaths.ModuleFields.TITLE),
            durationMinutes = numberField(raw, CourseFirestorePaths.ModuleFields.DURATION),
            order = numberField(raw, CourseFirestorePaths.ModuleFields.ORDER),
            lessons = listField(raw, CourseFirestorePaths.ModuleFields.LESSONS) { mapToLessonDocument(it) },
        )

    private fun mapToLessonDocument(raw: Map<String, Any?>): CourseLessonDocument =
        CourseLessonDocument(
            id = stringField(raw, CourseFirestorePaths.LessonFields.ID),
            title = stringField(raw, CourseFirestorePaths.LessonFields.TITLE),
            type = stringField(raw, CourseFirestorePaths.LessonFields.TYPE),
            durationMinutes = numberField(raw, CourseFirestorePaths.LessonFields.DURATION),
            xpReward = numberField(raw, CourseFirestorePaths.LessonFields.XP_REWARD),
            isFree = booleanField(raw, CourseFirestorePaths.LessonFields.IS_FREE),
            order = numberField(raw, CourseFirestorePaths.LessonFields.ORDER),
            videoUrl = stringField(raw, CourseFirestorePaths.LessonFields.VIDEO_URL),
            quizId = stringField(raw, CourseFirestorePaths.LessonFields.QUIZ_ID),
            contentData = mapField(raw, CourseFirestorePaths.LessonFields.CONTENT),
        )

    @Suppress("UNCHECKED_CAST")
    private fun mapField(data: Map<String, Any?>, key: String): Map<String, Any?>? {
        return (data[key] as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v }
    }

    private fun stringField(data: Map<String, Any?>, vararg keys: String): String? {
        for (key in keys) {
            val value = data[key] as? String
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun numberField(data: Map<String, Any?>, vararg keys: String): Long? {
        for (key in keys) {
            when (val value = data[key]) {
                is Number -> return value.toLong()
                is String -> return value.toLongOrNull()
            }
        }
        return null
    }

    private fun doubleField(data: Map<String, Any?>, vararg keys: String): Double? {
        for (key in keys) {
            when (val value = data[key]) {
                is Number -> return value.toDouble()
                is String -> return value.toDoubleOrNull()
            }
        }
        return null
    }

    private fun booleanField(data: Map<String, Any?>, vararg keys: String): Boolean? {
        for (key in keys) {
            when (val value = data[key]) {
                is Boolean -> return value
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> listField(
        data: Map<String, Any?>,
        key: String,
        transform: (Map<String, Any?>) -> T,
    ): List<T> {
        val raw = data[key] as? List<*> ?: return emptyList()
        return raw.mapNotNull { item ->
            (item as? Map<*, *>)?.entries
                ?.associate { (k, v) -> k.toString() to v }
                ?.let(transform)
        }
    }
}