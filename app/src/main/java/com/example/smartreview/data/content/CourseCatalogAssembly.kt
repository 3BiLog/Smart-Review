package com.example.smartreview.data.content

import com.example.smartreview.data.mock.MockLessonData
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.model.LessonContent
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.video.YouTubeVideoUrl

object CourseCatalogAssembly {

    fun lessonItem(lessonId: String, isLocked: Boolean): LessonItem {
        val content = MockLessonData.getLesson(lessonId)
            ?: error("Missing lesson content for id=$lessonId")
        val videoId = YouTubeVideoUrl.extractVideoId(content.videoUrl)
        return LessonItem(
            id = content.id,
            title = content.title,
            durationSeconds = content.estimatedMinutes * 60,
            thumbnailUrl = videoId?.let { YouTubeVideoUrl.thumbnailUrl(it) }
                ?: "https://picsum.photos/seed/${content.id}/320/180",
            isLocked = isLocked,
            videoUrl = content.videoUrl,
        )
    }

    fun module(
        id: String,
        title: String,
        lessonIds: List<String>,
    ): CourseModule {
        val lessons = lessonIds.map { lessonId ->
            lessonItem(lessonId, isLocked = false)
        }
        val totalMinutes = lessonIds.sumOf { MockLessonData.getLesson(it)?.estimatedMinutes ?: 0 }
        val durationLabel = "${lessons.size} bài · ${totalMinutes.coerceAtLeast(1)} phút"
        return CourseModule(
            id = id,
            title = title,
            lessonCount = lessons.size,
            durationLabel = durationLabel,
            lessons = lessons,
            isLocked = false,
        )
    }

    fun totalLessonCount(modules: List<CourseModule>): Int =
        modules.sumOf { it.lessons.size }
}
