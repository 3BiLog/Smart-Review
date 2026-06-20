package com.example.smartreview.data.learning

import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.model.FlashcardProgressSnapshot
import com.example.smartreview.data.model.LearningActivityType
import com.example.smartreview.data.model.LearningProgressionItem
import com.example.smartreview.data.model.LessonContent
import com.example.smartreview.data.model.LessonItem
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.model.QuizProgressSnapshot
import com.example.smartreview.data.model.UserLearningProgress
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.FlashcardRepositoryProvider
import com.example.smartreview.data.repository.LessonRepository
import com.example.smartreview.data.repository.LessonRepositoryProvider
import com.example.smartreview.data.repository.QuizRepositoryProvider
import com.example.smartreview.ui.navigation.LearningFlowNavigation
import com.example.smartreview.ui.screens.lesson.lessonContentRoute
import com.example.smartreview.ui.screens.quiz.quizRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LearningProgressionResolver(
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
    private val lessonRepository: LessonRepository = LessonRepositoryProvider.default,
) {

    private val courses: List<Course> by lazy {
        runBlocking { courseRepository.getAllCourses().first() }
    }

    fun resolveFromProgress(progress: UserLearningProgress): List<LearningProgressionItem> {
        val items = buildList {
            progress.flashcardInProgress
                ?.takeIf { runBlocking { ResumeLearningSupport.isFlashcardSnapshotResumable(it) } }
                ?.let { add(resolveFlashcard(it)) }
            progress.lessonInProgress
                ?.takeIf { runBlocking { ResumeLearningSupport.isLessonSnapshotResumable(it, progress) } }
                ?.let { add(resolveLesson(it, progress)) }
            progress.quizInProgress
                ?.takeIf { runBlocking { ResumeLearningSupport.isQuizSnapshotResumable(it, progress) } }
                ?.let { add(resolveQuiz(it, progress)) }
        }
        return items.filterNotNull().sortedByDescending { it.lastActivityAt }
    }

    fun resolveFlashcard(snapshot: FlashcardProgressSnapshot): LearningProgressionItem? {
        if (!runBlocking { ResumeLearningSupport.isFlashcardSnapshotResumable(snapshot) }) return null
        val deck = runBlocking { FlashcardRepositoryProvider.default.getDeck(snapshot.deckId) } ?: return null
        val total = deck.cards.size.coerceAtLeast(1)
        val studied = snapshot.knownCount + snapshot.reviewCount
        val route = "flashcard/${snapshot.deckId}"
        return LearningProgressionItem(
            type = LearningActivityType.FLASHCARD,
            contentId = snapshot.deckId,
            title = deck.title,
            progressPercent = studied.toFloat() / total,
            imageUrl = "https://picsum.photos/seed/flashcard_${snapshot.deckId}/400/200",
            route = route,
            courseTitle = "Ôn tập flashcard",
            progressDetail = "${studied}/${total} thẻ",
            lastActivityAt = snapshot.sessionStartedAt,
        )
    }

    fun resolveLesson(
        snapshot: LessonProgressSnapshot,
        progress: UserLearningProgress,
    ): LearningProgressionItem? {
        if (!runBlocking { ResumeLearningSupport.isLessonSnapshotResumable(snapshot, progress) }) return null
        val lesson = runBlocking { lessonRepository.getLesson(snapshot.lessonId) } ?: return null
        val contentBlocks = LessonContentBlocks.contentBlocks(lesson)
        val contentIds = contentBlocks.map { it.id }.toSet()
        val viewedContent = snapshot.viewedBlockIds.intersect(contentIds).size
        val totalContent = contentBlocks.size.coerceAtLeast(1)
        val route = if (viewedContent > 0) {
            lessonContentRoute(snapshot.lessonId)
        } else {
            LearningFlowNavigation.lessonVideoRoute(snapshot.lessonId)
        }
        val placement = findLessonPlacement(snapshot.lessonId)
        if (placement != null) {
            val (course, module, _) = placement
            return LearningProgressionItem(
                type = LearningActivityType.LESSON,
                contentId = snapshot.lessonId,
                title = lesson.title,
                progressPercent = viewedContent.toFloat() / totalContent,
                imageUrl = course.imageUrl,
                route = route,
                courseId = course.id,
                courseTitle = course.title,
                moduleId = module.id,
                moduleTitle = module.title,
                lessonId = lesson.id,
                progressDetail = "Nội dung ${viewedContent}/${totalContent}",
                lastActivityAt = snapshot.sessionStartedAt,
            )
        }
        return lessonOnlyItem(snapshot, lesson, viewedContent, totalContent, route)
    }

    fun resolveQuiz(
        snapshot: QuizProgressSnapshot,
        progress: UserLearningProgress,
    ): LearningProgressionItem? {
        if (!runBlocking { ResumeLearningSupport.isQuizSnapshotResumable(snapshot, progress) }) return null
        val quiz = runBlocking { QuizRepositoryProvider.default.getQuiz(snapshot.quizId) } ?: return null
        val lessonPlacement = quiz.lessonId
            ?.takeIf { it.isNotBlank() }
            ?.let { findLessonPlacement(it) }
        val total = quiz.questions.size.coerceAtLeast(1)
        val answered = snapshot.answers.size
        val (course, module) = if (lessonPlacement != null) {
            lessonPlacement.first to lessonPlacement.second
        } else {
            null to null
        }
        return LearningProgressionItem(
            type = LearningActivityType.QUIZ,
            contentId = snapshot.quizId,
            title = quiz.title,
            progressPercent = answered.toFloat() / total,
            imageUrl = course?.imageUrl ?: "https://picsum.photos/seed/quiz_${snapshot.quizId}/400/200",
            route = quizRoute(snapshot.quizId),
            courseId = course?.id,
            courseTitle = course?.title,
            moduleId = module?.id,
            moduleTitle = module?.title,
            lessonId = quiz.lessonId,
            progressDetail = "Quiz ${answered}/${total} câu",
            lastActivityAt = snapshot.sessionStartedAt,
        )
    }

    private fun lessonOnlyItem(
        snapshot: LessonProgressSnapshot,
        lesson: LessonContent,
        viewedContent: Int,
        totalContent: Int,
        route: String,
    ): LearningProgressionItem {
        val course = courses.find { it.id == lesson.courseId }
        val module = course?.modules?.find { it.id == lesson.moduleId }
        return LearningProgressionItem(
            type = LearningActivityType.LESSON,
            contentId = snapshot.lessonId,
            title = lesson.title,
            progressPercent = viewedContent.toFloat() / totalContent,
            imageUrl = course?.imageUrl ?: "https://picsum.photos/seed/lesson_${snapshot.lessonId}/400/200",
            route = route,
            courseId = lesson.courseId,
            courseTitle = course?.title,
            moduleId = lesson.moduleId,
            moduleTitle = module?.title ?: lesson.subtitle,
            lessonId = lesson.id,
            progressDetail = "Nội dung ${viewedContent}/${totalContent}",
            lastActivityAt = snapshot.sessionStartedAt,
        )
    }

    fun findLessonPlacement(lessonId: String): Triple<Course, CourseModule, LessonItem>? {
        for (course in courses) {
            for (module in course.modules) {
                val lesson = module.lessons.find { it.id == lessonId } ?: continue
                return Triple(course, module, lesson)
            }
        }
        return null
    }
}