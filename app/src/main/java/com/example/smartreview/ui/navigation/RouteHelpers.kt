package com.example.smartreview.ui.navigation

import android.util.Log

/** Centralized route helpers for learning flow routes. */
object RouteHelpers {
    const val LESSON_PLAYER_ROUTE_LEGACY = "lesson_player/{lessonId}"
    const val LESSON_PLAYER_ROUTE = "lesson_player/{courseId}/{lessonId}"
    private const val TAG = "RouteHelpers"

    fun lessonPlayerRoute(lessonId: String): String = "lesson_player/$lessonId"
    
    fun lessonPlayerRoute(courseId: String, lessonId: String): String {
        val route = "lesson_player/$courseId/$lessonId"
        Log.d(TAG, "lessonPlayerRoute: courseId=$courseId, lessonId=$lessonId → $route")
        return route
    }
}
