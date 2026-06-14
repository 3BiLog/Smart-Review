package com.example.smartreview.data.learning

import com.example.smartreview.data.lesson.LessonVideoEnrichment
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.model.LessonBlockType
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.repository.LessonRepositoryProvider

class LearningProgressionPolicy(
    private val lessonRepository: LessonRepository = LessonRepositoryProvider.default,
) {

    data class ProgressSnapshot(
        val completedLessonIds: Set<String> = emptySet(),
        val completedQuizIds: Set<String> = emptySet(),
    )

    data class CourseProgressResult(
        val course: Course,
        val progressFraction: Float,
        val completedLessonCount: Int,
        val totalLessonCount: Int,
        val recommendedNextLessonId: String? = null,
        val recommendedNextLessonTitle: String? = null,
    )

    fun applyToCourse(template: Course, snapshot: ProgressSnapshot): CourseProgressResult {
        val orderedLessonIds = template.modules.flatMap { module ->
            module.lessons.map { it.id }
        }
        val total = orderedLessonIds.size.coerceAtLeast(1)

        // FIXED: Always unlock all modules and lessons
        val modules = template.modules.mapIndexed { moduleIndex, module ->
            val lessons = module.lessons.mapIndexed { lessonIndex, lesson ->
                LessonVideoEnrichment.enrich(lesson).copy(isLocked = false)  // Always unlocked
            }
            module.copy(
                isLocked = false,  // Always unlocked
                lessons = lessons,
            )
        }

        val completedCount = orderedLessonIds.count { isLessonFullyComplete(it, snapshot) }
        val (nextId, nextTitle) = findRecommendedNext(modules, snapshot)

        return CourseProgressResult(
            course = template.copy(
                modules = modules,
                progress = completedCount.toFloat() / total,
            ),
            progressFraction = completedCount.toFloat() / total,
            completedLessonCount = completedCount,
            totalLessonCount = total,
            recommendedNextLessonId = nextId,
            recommendedNextLessonTitle = nextTitle,
        )
    }

    fun isLessonFullyComplete(lessonId: String, snapshot: ProgressSnapshot): Boolean {
        if (lessonId !in snapshot.completedLessonIds) return false
        val requiredQuizzes = requiredQuizIdsForLesson(lessonId)
        if (requiredQuizzes.isEmpty()) return true
        return requiredQuizzes.all { it in snapshot.completedQuizIds }
    }

    fun requiredQuizIdsForLesson(lessonId: String): Set<String> {
        val lesson = lessonRepository.getLesson(lessonId) ?: return emptySet()
        return lesson.blocks
            .filter { it.type == LessonBlockType.QUIZ_STUB }
            .mapNotNull { it.quizStubId?.takeIf { id -> id.isNotBlank() } }
            .toSet()
    }

    // FIXED: Always return true - no locking
    private fun isModuleUnlocked(
        template: Course,
        moduleIndex: Int,
        snapshot: ProgressSnapshot,
    ): Boolean = true  // Always unlocked

    private fun isLessonUnlocked(
        module: CourseModule,
        lessonIndex: Int,
        snapshot: ProgressSnapshot,
    ): Boolean = true  // Always unlocked

    private fun findRecommendedNext(
        modules: List<CourseModule>,
        snapshot: ProgressSnapshot,
    ): Pair<String?, String?> {
        for (module in modules) {
            for (lesson in module.lessons) {
                if (!isLessonFullyComplete(lesson.id, snapshot)) {
                    return lesson.id to lesson.title
                }
            }
        }
        return null to null
    }
}