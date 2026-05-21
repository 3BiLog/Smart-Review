package com.example.smartreview.ui.screens.lessonsummary

const val LESSON_SUMMARY_ROUTE = "lesson_summary/{sessionId}"

fun lessonSummaryRoute(sessionId: String): String = "lesson_summary/$sessionId"
