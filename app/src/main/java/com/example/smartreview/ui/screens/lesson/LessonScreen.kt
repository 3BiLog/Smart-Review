package com.example.smartreview.ui.screens.lesson

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/** Legacy entry — delegates to [LessonContentScreen]. */
@Composable
fun LessonScreen(
    navController: NavHostController,
    lessonId: String,
) {
    LessonContentScreen(navController = navController, lessonId = lessonId)
}
