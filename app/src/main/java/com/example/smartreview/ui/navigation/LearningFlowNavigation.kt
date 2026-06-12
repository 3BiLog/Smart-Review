package com.example.smartreview.ui.navigation

import androidx.navigation.NavHostController
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.ui.screens.coursedetail.courseDetailRoute
import com.example.smartreview.ui.screens.lesson.lessonContentRoute
import com.example.smartreview.ui.screens.lessonplayer.lessonPlayerRoute
import com.example.smartreview.ui.navigation.Screen

/**
 * Canonical routing for the separated learning flow:
 *
 * 1. Course progression — [resolveNextUnlockedLesson], [resolveFirstUnlockedLesson]
 * 2. Video — [lessonVideoRoute] / [navigateLessonVideo]
 * 3. Theory / summary — [lessonContentRoute] / [navigateLessonContent]
 * 4. Quiz — quiz routes (see [com.example.smartreview.ui.screens.quiz.quizRoute])
 * 5. Completion — lesson_summary / course return helpers
 */
object LearningFlowNavigation {

    /** Always enter a lesson through the video screen (placeholder if no URL). */
    fun lessonVideoRoute(lessonId: String): String = lessonPlayerRoute(lessonId)

    /** @deprecated Use [lessonVideoRoute]; kept for resume/progression callers. */
    fun lessonEntryRoute(lessonId: String): String = lessonVideoRoute(lessonId)

    fun resolveNextUnlockedLesson(
        course: Course,
        preferredLessonId: String? = null,
    ): LessonItem? {
        val lessons = course.modules.flatMap { it.lessons }
        preferredLessonId?.let { id ->
            lessons.find { it.id == id && !it.isLocked }?.let { return it }
        }
        return lessons.firstOrNull { !it.isLocked }
    }

    /** First unlocked lesson in catalog order (Hero Play). */
    fun resolveFirstUnlockedLesson(course: Course): LessonItem? =
        course.modules.flatMap { it.lessons }.firstOrNull { !it.isLocked }

    /**
     * Navigate to lesson video. If courseId is known, build canonical route with courseId.
     */
    fun NavHostController.navigateLessonVideo(lessonId: String, courseId: String? = null) {
        if (!courseId.isNullOrBlank()) {
            navigate(RouteHelpers.lessonPlayerRoute(courseId, lessonId)) {
                launchSingleTop = true
            }
            return
        }
        navigate(lessonVideoRoute(lessonId)) {
            launchSingleTop = true
        }
    }

    /** @deprecated Use [navigateLessonVideo]. */
    fun NavHostController.navigateLessonEntry(lessonId: String) = navigateLessonVideo(lessonId)

    fun NavHostController.navigateLessonContent(lessonId: String) {
        navigate(lessonContentRoute(lessonId)) {
            launchSingleTop = true
        }
    }

    /** Hero: first unlocked lesson in the course — route by lesson type. */
    fun NavHostController.navigateHeroPlay(course: Course) {
        val lesson = resolveFirstUnlockedLesson(course) ?: return
        // include course context for canonical resolution
        when (lesson.lessonType) {
            com.example.smartreview.data.model.LessonType.VIDEO, com.example.smartreview.data.model.LessonType.UNKNOWN -> navigateLessonVideo(lesson.id, courseId = course.id)
            com.example.smartreview.data.model.LessonType.READING -> navigateLessonContent(lesson.id)
            com.example.smartreview.data.model.LessonType.QUIZ -> navigate(com.example.smartreview.ui.screens.quiz.quizRoute(lesson.quizId ?: lesson.id)) { launchSingleTop = true }
            com.example.smartreview.data.model.LessonType.FLASHCARD -> navigate(Screen.Flashcard.route) { launchSingleTop = true }
        }
    }
 
    /**
     * Start / Continue: next incomplete unlocked lesson (or preferred), route by lesson type.
     */
    fun NavHostController.navigateStartLearning(
        course: Course,
        preferredLessonId: String? = null,
    ) {
        val lesson = resolveNextUnlockedLesson(course, preferredLessonId) ?: return
        when (lesson.lessonType) {
            com.example.smartreview.data.model.LessonType.VIDEO, com.example.smartreview.data.model.LessonType.UNKNOWN -> navigateLessonVideo(lesson.id, courseId = course.id)
            com.example.smartreview.data.model.LessonType.READING -> navigateLessonContent(lesson.id)
            com.example.smartreview.data.model.LessonType.QUIZ -> navigate(com.example.smartreview.ui.screens.quiz.quizRoute(lesson.quizId ?: lesson.id)) { launchSingleTop = true }
            com.example.smartreview.data.model.LessonType.FLASHCARD -> navigate(Screen.Flashcard.route) { launchSingleTop = true }
        }
    }

    fun NavHostController.navigateToCourseFromStudy(courseId: String) {
        if (courseId.isBlank()) {
            popBackStack(Screen.Home.route, inclusive = false)
            return
        }
        navigate(courseDetailRoute(courseId)) {
            popUpTo(Screen.Home.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun NavHostController.navigateToHomeFromStudy() {
        popBackStack(Screen.Home.route, inclusive = false)
    }
}
