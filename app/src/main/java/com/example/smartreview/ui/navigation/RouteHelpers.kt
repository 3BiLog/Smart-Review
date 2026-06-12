package com.example.smartreview.ui.navigation

/** Centralized route helpers for learning flow routes. */
object RouteHelpers {
    const val LESSON_PLAYER_ROUTE_LEGACY = "lesson_player/{lessonId}"
    const val LESSON_PLAYER_ROUTE = "lesson_player/{courseId}/{lessonId}"

    fun lessonPlayerRoute(lessonId: String): String = "lesson_player/$lessonId"
    fun lessonPlayerRoute(courseId: String, lessonId: String): String = "lesson_player/$courseId/$lessonId"
}
