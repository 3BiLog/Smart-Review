package com.example.smartreview.data.content

/**
 * Future Firestore / admin web shape (not wired yet).
 *
 * ```
 * courses/{courseId}
 *   title, description, category, difficulty, price, ...
 * courses/{courseId}/modules/{moduleId}
 *   title, orderIndex, isLocked
 * courses/{courseId}/lessons/{lessonId}
 *   moduleId, orderIndex, title, videoUrl, blocks[], xpReward
 * quizzes/{quizId}
 *   lessonId, questions[]
 * ```
 *
 * App models today: [com.example.smartreview.data.model.Course],
 * [com.example.smartreview.data.model.LessonContent],
 * [com.example.smartreview.data.model.Quiz].
 */
object CourseContentSchema {
    const val COLLECTION_COURSES = "courses"
    const val SUBCOLLECTION_MODULES = "modules"
    const val SUBCOLLECTION_LESSONS = "lessons"
    const val COLLECTION_QUIZZES = "quizzes"
}
