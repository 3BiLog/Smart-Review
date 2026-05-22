package com.example.smartreview.ui.screens.lesson

const val LESSON_ROUTE = "lesson/{lessonId}"

/** Theory / summary content step (same destination as legacy [lessonRoute]). */
fun lessonContentRoute(lessonId: String): String = "lesson/$lessonId"

/** @deprecated Prefer [lessonContentRoute] for new learning-flow code. */
fun lessonRoute(lessonId: String): String = lessonContentRoute(lessonId)
