package com.example.smartreview.data.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeVideoUrlTest {

    @Test
    fun extractVideoId_watchUrl() {
        assertEquals(
            "LXb3EKWsInQ",
            YouTubeVideoUrl.extractVideoId("https://www.youtube.com/watch?v=LXb3EKWsInQ"),
        )
    }

    @Test
    fun extractVideoId_shortUrl() {
        assertEquals(
            "aqz-KE-bpKQ",
            YouTubeVideoUrl.extractVideoId("https://youtu.be/aqz-KE-bpKQ"),
        )
    }

    @Test
    fun extractVideoId_embedUrl() {
        assertEquals(
            "ScMzIvxBSi4",
            YouTubeVideoUrl.extractVideoId("https://www.youtube-nocookie.com/embed/ScMzIvxBSi4"),
        )
    }

    @Test
    fun extractVideoId_rawId() {
        assertEquals("dQw4w9WgXcQ", YouTubeVideoUrl.extractVideoId("dQw4w9WgXcQ"))
    }

    @Test
    fun extractVideoId_rejectsNonYouTube() {
        assertNull(YouTubeVideoUrl.extractVideoId("https://vimeo.com/12345"))
    }

    @Test
    fun embedHtml_containsVideoId() {
        val html = YouTubeVideoUrl.embedHtml("LXb3EKWsInQ")
        assertNotNull(html)
        assert(html.contains("LXb3EKWsInQ"))
        assert(html.contains("youtube-nocookie.com/embed"))
    }
}
