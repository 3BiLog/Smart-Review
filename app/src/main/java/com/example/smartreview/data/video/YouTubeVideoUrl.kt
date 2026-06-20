package com.example.smartreview.data.video

object YouTubeVideoUrl {

    private val VIDEO_ID_PATTERN = Regex("^[a-zA-Z0-9_-]{11}$")

    fun extractVideoId(urlOrId: String?): String? {
        val raw = urlOrId?.trim().orEmpty()
        if (raw.isEmpty()) return null
        if (VIDEO_ID_PATTERN.matches(raw)) return raw

        val normalized = raw
            .removePrefix("http://")
            .removePrefix("https://")
            .lowercase()

        if (!normalized.contains("youtube") && !normalized.startsWith("youtu.be")) {
            return null
        }

        val withoutQuery = raw.substringBefore("?").substringBefore("#")
        val path = withoutQuery
            .removePrefix("https://")
            .removePrefix("http://")
            .substringAfter("/", missingDelimiterValue = withoutQuery)

        return when {
            "youtu.be/" in withoutQuery.lowercase() -> {
                withoutQuery.substringAfterLast("/").substringBefore("?").takeIf { isValidId(it) }
            }
            "/embed/" in withoutQuery -> {
                withoutQuery.substringAfter("/embed/").substringBefore("?").substringBefore("/")
                    .takeIf { isValidId(it) }
            }
            "v=" in raw -> {
                Regex("[?&]v=([a-zA-Z0-9_-]{11})").find(raw)?.groupValues?.get(1)
            }
            else -> null
        }
    }

    fun embedHtml(videoId: String): String {
        val safeId = videoId.takeIf { isValidId(it) } ?: return ""
        val embedUrl =
            "https://www.youtube-nocookie.com/embed/$safeId?playsinline=1&rel=0&modestbranding=1&fs=1"
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <style>
                html, body { margin: 0; padding: 0; background: #000; height: 100%; }
                iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
              </style>
            </head>
            <body>
              <iframe
                src="$embedUrl"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowfullscreen
                referrerpolicy="strict-origin-when-cross-origin">
              </iframe>
            </body>
            </html>
        """.trimIndent()
    }

    fun thumbnailUrl(videoId: String): String =
        "https://img.youtube.com/vi/${videoId}/hqdefault.jpg"

    private fun isValidId(id: String): Boolean = VIDEO_ID_PATTERN.matches(id)
}
