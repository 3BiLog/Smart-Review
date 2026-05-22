package com.example.smartreview.data.learning

import com.example.smartreview.data.lesson.LessonVideoEnrichment
import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.model.LessonBlockType
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.repository.LessonRepositoryProvider

/**
 * Derives module/lesson unlock state and course progress from completed lessons/quizzes.
 */
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

        val modules = template.modules.mapIndexed { moduleIndex, module ->
            val moduleUnlocked = isModuleUnlocked(template, moduleIndex, snapshot)
            val lessons = module.lessons.mapIndexed { lessonIndex, lesson ->
                val lessonUnlocked = moduleUnlocked && isLessonUnlocked(
                    module = module,
                    lessonIndex = lessonIndex,
                    snapshot = snapshot,
                )
                LessonVideoEnrichment.enrich(lesson).copy(isLocked = !lessonUnlocked)
            }
            module.copy(
                isLocked = !moduleUnlocked,
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

    private fun isModuleUnlocked(
        template: Course,
        moduleIndex: Int,
        snapshot: ProgressSnapshot,
    ): Boolean {
        if (moduleIndex == 0) return true
        val previous = template.modules[moduleIndex - 1]
        return previous.lessons.all { isLessonFullyComplete(it.id, snapshot) }
    }

    private fun isLessonUnlocked(
        module: CourseModule,
        lessonIndex: Int,
        snapshot: ProgressSnapshot,
    ): Boolean {
        if (lessonIndex == 0) return true
        val previousLessonId = module.lessons[lessonIndex - 1].id
        return isLessonFullyComplete(previousLessonId, snapshot)
    }

    private fun findRecommendedNext(
        modules: List<CourseModule>,
        snapshot: ProgressSnapshot,
    ): Pair<String?, String?> {
        for (module in modules) {
            if (module.isLocked) break
            for (lesson in module.lessons) {
                if (!lesson.isLocked && !isLessonFullyComplete(lesson.id, snapshot)) {
                    return lesson.id to lesson.title
                }
            }
        }
        return null to null
    }
}
