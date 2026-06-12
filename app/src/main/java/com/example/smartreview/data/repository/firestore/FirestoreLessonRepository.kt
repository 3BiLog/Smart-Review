package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.LessonBlock
import com.example.smartreview.data.model.LessonContent
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.remote.firestore.CourseFirestoreMapper
import com.example.smartreview.data.remote.firestore.CourseFirestorePaths
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.repository.CourseCache
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks

/**
 * Firestore-backed LessonRepository implemented using existing DA3 schema.
 * This implementation performs a best-effort lookup by scanning published
 * courses and matching embedded lesson ids. It populates CourseCache so
 * repeated lookups are cheaper.
 */
class FirestoreLessonRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : LessonRepository {

    override fun getLesson(lessonId: String): LessonContent? {
        if (lessonId.isBlank()) return null

        return try {
            val task = firestore.collection(CourseFirestorePaths.COURSES)
                .whereEqualTo(CourseFirestorePaths.Fields.STATUS, "published")
                .get()
            val snapshot = Tasks.await(task)
            for (doc in snapshot.documents) {
                val course = CourseFirestoreMapper.toCourse(doc.id, doc.data)
                if (course != null) {
                    CourseCache.put(course)
                    for (module in course.modules) {
                        for (lesson in module.lessons) {
                            if (lesson.id == lessonId) {
                                return lessonItemToContent(lesson, course.id, module.id)
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    override fun getLessonsForCourse(courseId: String): List<LessonContent> {
        if (courseId.isBlank()) return emptyList()
        val cachedCourse = CourseCache.get(courseId)
        val course = if (cachedCourse != null) cachedCourse else try {
            val task = firestore.collection(CourseFirestorePaths.COURSES)
                .document(courseId)
                .get()
            val doc = Tasks.await(task)
            if (!doc.exists()) return emptyList()
            CourseFirestoreMapper.toCourse(doc.id, doc.data)
        } catch (e: Exception) {
            null
        }
        if (course == null) return emptyList()
        CourseCache.put(course)
        return course.modules.flatMapIndexed { moduleIndex, module ->
            module.lessons.map { lesson ->
                lessonItemToContent(lesson, course.id, module.id)
            }
        }
    }

    // Helpers
    private fun lessonItemToContent(lesson: LessonItem, courseId: String?, moduleId: String? = null): LessonContent {
        return LessonContent(
            id = lesson.id,
            courseId = courseId ?: "",
            moduleId = moduleId ?: "",
            orderInModule = 0,
            title = lesson.title,
            subtitle = "",
            estimatedMinutes = lesson.durationSeconds / 60,
            blocks = parseBlocks(lesson),
            videoUrl = lesson.videoUrl,
            xpReward = lesson.xpReward,
        )
    }

    private fun parseBlocks(lesson: LessonItem): List<LessonBlock> {
        val contentObject = lesson.contentData?.get(CourseFirestorePaths.ContentFields.DATA) as? Map<*, *>
        val contentData = contentObject?.entries?.associate { (k, v) -> k.toString() to v } ?: return emptyList()
        val markdown = stringField(contentData, CourseFirestorePaths.ContentFields.MARKDOWN, CourseFirestorePaths.ContentFields.TEXT)
        if (!markdown.isNullOrBlank()) {
            return listOf(
                LessonBlock(
                    id = "block_markdown_${lesson.id}",
                    type = com.example.smartreview.data.model.LessonBlockType.TEXT,
                    body = markdown.trim(),
                ),
            )
        }
        return emptyList()
    }

    private fun stringField(data: Map<String, Any?>, vararg keys: String): String? {
        for (key in keys) {
            val value = data[key] as? String
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

}

