package com.example.smartreview.ui.navigation

import androidx.navigation.NavHostController
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.model.LessonType
import com.example.smartreview.ui.screens.coursedetail.courseDetailRoute
import com.example.smartreview.ui.screens.lesson.lessonContentRoute
import com.example.smartreview.ui.screens.lessonplayer.lessonPlayerRoute
import com.example.smartreview.ui.navigation.Screen
import com.example.smartreview.ui.screens.reading.readingRoute

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
            val route = RouteHelpers.lessonPlayerRoute(courseId, lessonId)
            android.util.Log.d("LearningFlowNavigation", "navigateLessonVideo: courseId=$courseId, lessonId=$lessonId, route=$route")
            navigate(route) {
                launchSingleTop = false
                restoreState = false
            }
            return
        }
        val route = lessonVideoRoute(lessonId)
        android.util.Log.d("LearningFlowNavigation", "navigateLessonVideo (no course): lessonId=$lessonId, route=$route")
        navigate(route) {
            launchSingleTop = false
            restoreState = false
        }
    }

    /** @deprecated Use [navigateLessonVideo]. */
    fun NavHostController.navigateLessonEntry(lessonId: String) = navigateLessonVideo(lessonId)

    fun NavHostController.navigateReading(lessonId: String) {
        navigate(readingRoute(lessonId)) {
            launchSingleTop = true
        }
    }

    fun NavHostController.navigateLessonContent(lessonId: String) {
        navigate(lessonContentRoute(lessonId)) {
            launchSingleTop = true
        }
    }

    /** Hero: first unlocked lesson in the course — route by lesson type. */
    fun NavHostController.navigateHeroPlay(course: Course) {
        val lesson = resolveFirstUnlockedLesson(course) ?: return
        when (lesson.lessonType) {
            LessonType.VIDEO, LessonType.UNKNOWN -> navigateLessonVideo(lesson.id, courseId = course.id)
            LessonType.READING -> navigateLessonContent(lesson.id)
            LessonType.QUIZ -> navigate(com.example.smartreview.ui.screens.quiz.quizRoute(lesson.quizId ?: lesson.id)) { launchSingleTop = true }
            LessonType.FLASHCARD -> {
                navigate("flashcard/${lesson.id}") { launchSingleTop = true }
            }
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
            LessonType.VIDEO, LessonType.UNKNOWN -> navigateLessonVideo(lesson.id, courseId = course.id)
            LessonType.READING -> navigateLessonContent(lesson.id)
            LessonType.QUIZ -> navigate(com.example.smartreview.ui.screens.quiz.quizRoute(lesson.quizId ?: lesson.id)) { launchSingleTop = true }
            LessonType.FLASHCARD -> {
                navigate("flashcard/${lesson.id}") { launchSingleTop = true }
            }
        }
    }

    /**
     * ✅ Navigate to the next uncompleted lesson in course (for Continue Learning)
     * @param course The course to continue
     * @param nextLessonId The ID of the next lesson to complete (can be null if course is completed)
     */
    fun NavHostController.navigateContinueLearning(course: Course, nextLessonId: String?) {
        if (nextLessonId != null) {
            android.util.Log.d("LearningFlowNavigation", "navigateContinueLearning: course=${course.title}, nextLessonId=$nextLessonId")
            navigateLessonVideo(nextLessonId, courseId = course.id)
        } else {
            // Course is completed, navigate to course detail
            android.util.Log.d("LearningFlowNavigation", "navigateContinueLearning: course completed, navigate to detail")
            navigate(courseDetailRoute(course.id))
        }
    }

    /**
     * ✅ Navigate to course detail screen
     */
    fun NavHostController.navigateToCourseDetail(courseId: String) {
        navigate(courseDetailRoute(courseId)) {
            launchSingleTop = true
            restoreState = true
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

    /**
     * ✅ Navigate to a specific lesson by ID with optional course context
     */
    fun NavHostController.navigateToLesson(lessonId: String, courseId: String? = null) {
        val lessonType = getLessonType(lessonId)
        when (lessonType) {
            LessonType.VIDEO, LessonType.UNKNOWN -> navigateLessonVideo(lessonId, courseId)
            LessonType.READING -> navigateLessonContent(lessonId)
            LessonType.QUIZ -> navigate(com.example.smartreview.ui.screens.quiz.quizRoute(lessonId)) { launchSingleTop = true }
            LessonType.FLASHCARD -> navigate("flashcard/${lessonId}") { launchSingleTop = true }
        }
    }

    /**
     * ✅ Helper function to get lesson type (placeholder - should be implemented with actual data)
     */
    private fun getLessonType(lessonId: String): LessonType {
        // This should be implemented to fetch from repository
        // For now, default to VIDEO
        return LessonType.VIDEO
    }

    /**
     * ✅ Navigate and clear back stack (useful for logout or reset)
     */
    fun NavHostController.navigateAndClear(route: String) {
        navigate(route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    /**
     * ✅ Pop back to home screen
     */
    fun NavHostController.popToHome() {
        popBackStack(Screen.Home.route, inclusive = false)
    }
}