package com.example.smartreview.data.lesson

import com.example.smartreview.data.mock.MockLessonData
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.video.YouTubeVideoUrl

object LessonVideoEnrichment {

    fun enrich(item: LessonItem): LessonItem {
        val videoUrl = MockLessonData.getLesson(item.id)?.videoUrl.orEmpty()
        if (videoUrl.isBlank()) return item
        val videoId = YouTubeVideoUrl.extractVideoId(videoUrl)
        return item.copy(
            videoUrl = videoUrl,
            thumbnailUrl = videoId?.let { YouTubeVideoUrl.thumbnailUrl(it) } ?: item.thumbnailUrl,
        )
    }
}
