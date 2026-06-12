package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizOption
import com.example.smartreview.data.model.QuizQuestion
import com.example.smartreview.data.remote.firestore.CourseFirestoreMapper
import com.example.smartreview.data.remote.firestore.CourseFirestorePaths
import com.example.smartreview.data.repository.CourseCache
import com.example.smartreview.data.repository.QuizRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreQuizRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : QuizRepository {

    override fun getQuiz(quizId: String): Quiz? {
        if (quizId.isBlank()) return null

        return try {
            val task = firestore.collection(CourseFirestorePaths.COURSES)
                .whereEqualTo(CourseFirestorePaths.Fields.STATUS, "published")
                .get()
            val snapshot = Tasks.await(task)
            for (doc in snapshot.documents) {
                val course = CourseFirestoreMapper.toCourse(doc.id, doc.data)
                if (course != null) {
                    CourseCache.put(course)
                    for (module in course.modules) {
                        for (lesson in module.lessons) {
                            if (matchesQuizId(lesson, quizId)) {
                                return lessonToQuiz(lesson)
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun matchesQuizId(lesson: com.example.smartreview.data.model.LessonItem, quizId: String): Boolean {
        return lesson.quizId == quizId || lesson.id == quizId
    }

    private fun lessonToQuiz(lesson: com.example.smartreview.data.model.LessonItem): Quiz? {
        val contentData = extractContentData(lesson.contentData) ?: return null
        val questionMaps = listField(contentData, CourseFirestorePaths.ContentFields.QUESTIONS)
        val questions = questionMaps.mapIndexedNotNull { index, rawQuestion -> mapQuestion(rawQuestion, index) }
        if (questions.isEmpty()) return null

        return Quiz(
            id = lesson.quizId ?: lesson.id,
            title = lesson.title.ifBlank { "Quiz" },
            subtitle = "Quiz trên bài học",
            lessonId = lesson.id,
            questions = questions,
        )
    }

    private fun mapQuestion(raw: Map<String, Any?>, index: Int): QuizQuestion? {
        val questionId = stringField(raw, CourseFirestorePaths.ContentFields.ID) ?: "q${index + 1}"
        val prompt = stringField(raw, CourseFirestorePaths.ContentFields.PROMPT, CourseFirestorePaths.ContentFields.TITLE) ?: return null
        val explanation = stringField(raw, CourseFirestorePaths.ContentFields.EXPLANATION) ?: ""
        val options = listField(raw, CourseFirestorePaths.ContentFields.OPTIONS).mapIndexedNotNull { optIndex, rawOption ->
            mapOption(rawOption, questionId, optIndex)
        }
        val correctOptionId = stringField(raw, CourseFirestorePaths.ContentFields.CORRECT) ?: ""
        if (options.isEmpty()) return null

        return QuizQuestion(
            id = questionId,
            prompt = prompt,
            options = options,
            correctOptionId = correctOptionId.ifBlank { options.first().id },
            explanation = explanation,
        )
    }

    private fun mapOption(raw: Map<String, Any?>, questionId: String, index: Int): QuizOption? {
        val optionId = stringField(raw, CourseFirestorePaths.ContentFields.ID) ?: "${questionId}_opt_${index + 1}"
        val label = stringField(raw, CourseFirestorePaths.ContentFields.LABEL, CourseFirestorePaths.ContentFields.ANSWER, CourseFirestorePaths.ContentFields.TITLE)
            ?: raw[CourseFirestorePaths.ContentFields.LABEL]?.toString()
            ?: return null
        return QuizOption(id = optionId, label = label)
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractContentData(contentData: Map<String, Any?>?): Map<String, Any?>? {
        return (contentData?.get(CourseFirestorePaths.ContentFields.DATA) as? Map<*, *>)
            ?.entries
            ?.associate { it.key.toString() to it.value }
    }

    @Suppress("UNCHECKED_CAST")
    private fun listField(data: Map<String, Any?>, key: String): List<Map<String, Any?>> {
        val rawList = data[key] as? List<*> ?: return emptyList()
        return rawList.mapNotNull { element ->
            (element as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value }
        }
    }

    private fun stringField(data: Map<String, Any?>, vararg keys: String): String? {
        for (key in keys) {
            val value = data[key] as? String
            if (!value.isNullOrBlank()) return value
        }
        return null
    }
}
