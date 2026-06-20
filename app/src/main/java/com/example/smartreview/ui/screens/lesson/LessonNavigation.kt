package com.example.smartreview.ui.screens.lesson

const val LESSON_ROUTE = "lesson/{lessonId}"

fun lessonContentRoute(lessonId: String): String = "lesson/$lessonId"

fun lessonRoute(lessonId: String): String = lessonContentRoute(lessonId)
