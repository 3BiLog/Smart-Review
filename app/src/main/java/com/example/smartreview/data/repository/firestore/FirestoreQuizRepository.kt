package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizQuestion
import com.example.smartreview.data.remote.firestore.CourseFirestoreMapper
import com.example.smartreview.data.remote.firestore.CourseFirestorePaths
import com.example.smartreview.data.repository.CourseCache
import com.example.smartreview.data.repository.QuizRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreQuizRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : QuizRepository {

    override suspend fun getQuiz(quizId: String): Quiz? = withContext(Dispatchers.IO) {
        if (quizId.isBlank()) return@withContext null

        android.util.Log.d("FirestoreQuizRepository", "Looking for quiz with ID: $quizId")

        return@withContext try {
            val snapshot = firestore.collection(CourseFirestorePaths.COURSES)
                .whereEqualTo(CourseFirestorePaths.Fields.STATUS, "published")
                .get()
                .await()

            android.util.Log.d("FirestoreQuizRepository", "Checking ${snapshot.documents.size} courses")

            for (doc in snapshot.documents) {
                val course = CourseFirestoreMapper.toCourse(doc.id, doc.data)
                if (course != null) {
                    CourseCache.put(course)
                    for (module in course.modules) {
                        for (lesson in module.lessons) {
                            if (matchesQuizId(lesson, quizId)) {
                                android.util.Log.d("FirestoreQuizRepository", "Found quiz lesson: ${lesson.id} in course ${course.id}")
                                return@withContext lessonToQuiz(lesson, course.id)
                            }
                        }
                    }
                }
            }
            android.util.Log.d("FirestoreQuizRepository", "Quiz not found: $quizId")
            null
        } catch (e: Exception) {
            android.util.Log.e("FirestoreQuizRepository", "Error finding quiz", e)
            null
        }
    }

    private fun matchesQuizId(lesson: com.example.smartreview.data.model.LessonItem, quizId: String): Boolean {
        return lesson.quizId == quizId || lesson.id == quizId
    }

    private fun lessonToQuiz(lesson: com.example.smartreview.data.model.LessonItem, courseId: String): Quiz? {
        val contentData = extractContentData(lesson.contentData) ?: return null
        val questionMaps = listField(contentData, CourseFirestorePaths.ContentFields.QUESTIONS)
        val questions = questionMaps.mapIndexedNotNull { index, rawQuestion -> mapQuestion(rawQuestion, index) }
        if (questions.isEmpty()) {
            android.util.Log.d("FirestoreQuizRepository", "No questions found for quiz: ${lesson.id}")
            return null
        }

        android.util.Log.d("FirestoreQuizRepository", "Loaded ${questions.size} questions for quiz: ${lesson.id}")

        return Quiz(
            id = lesson.quizId ?: lesson.id,
            title = lesson.title.ifBlank { "Quiz" },
            description = "Quiz trên bài học",
            lessonId = lesson.id,
            courseId = courseId,
            moduleId = null,
            questions = questions,
            passingScore = extractPassingScore(contentData),
            xpReward = lesson.xpReward.toLong(),
            duration = 0,
        )
    }

    private fun extractPassingScore(contentData: Map<String, Any?>): Int {
        return (contentData["passingScore"] as? Number)?.toInt() ?: 70
    }

    private fun mapQuestion(raw: Map<String, Any?>, index: Int): QuizQuestion? {
        val questionId = stringField(raw, CourseFirestorePaths.ContentFields.ID) ?: "q${index + 1}"

        val text = stringField(
            raw,
            "text",
            CourseFirestorePaths.ContentFields.PROMPT,
            CourseFirestorePaths.ContentFields.TITLE
        ) ?: return null

        val explanation = stringField(raw, CourseFirestorePaths.ContentFields.EXPLANATION) ?: ""

        val options = parseOptionsAsStrings(raw)
        if (options.isEmpty()) return null

        val correctOptionIndex = parseCorrectOptionIndex(raw, options.size)

        return QuizQuestion(
            id = questionId,
            text = text,
            options = options,
            correctOptionIndex = correctOptionIndex,
            explanation = explanation,
        )
    }

    private fun parseOptionsAsStrings(raw: Map<String, Any?>): List<String> {
        val optionsList = raw[CourseFirestorePaths.ContentFields.OPTIONS] ?: return emptyList()

        return when {
            optionsList is List<*> && optionsList.isNotEmpty() && optionsList.first() is String -> {
                optionsList.filterIsInstance<String>()
            }
            optionsList is List<*> -> {
                optionsList.mapNotNull { obj ->
                    when (obj) {
                        is String -> obj
                        is Map<*, *> -> (obj["label"] ?: obj["answer"] ?: obj["title"]) as? String
                        else -> null
                    }
                }
            }
            else -> emptyList()
        }
    }

    private fun parseCorrectOptionIndex(raw: Map<String, Any?>, optionsSize: Int): Int {
        val correctIndex = (raw["correctOptionIndex"] as? Number)?.toInt()
        if (correctIndex != null && correctIndex >= 0 && correctIndex < optionsSize) {
            return correctIndex
        }

        val correctValue = stringField(raw, CourseFirestorePaths.ContentFields.CORRECT)
        if (correctValue != null) {
            val index = correctValue.toIntOrNull()
            if (index != null && index >= 0 && index < optionsSize) {
                return index
            }
        }

        return 0
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