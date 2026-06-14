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
        
        // CHANGE 1: Support both "text" (new schema) and "prompt"/"title" (old schema)
        val prompt = stringField(
            raw, 
            "text",  // NEW: Firestore Web Admin uses "text"
            CourseFirestorePaths.ContentFields.PROMPT,  // OLD: Mock/legacy uses "prompt"
            CourseFirestorePaths.ContentFields.TITLE     // OLD: Fallback to "title"
        ) ?: return null
        
        val explanation = stringField(raw, CourseFirestorePaths.ContentFields.EXPLANATION) ?: ""
        
        // CHANGE 2: Parse options—support BOTH string array and object array
        val options = parseOptions(raw, questionId, index)
        if (options.isEmpty()) return null
        
        // CHANGE 3: Support both correctOptionIndex (new) and correct (old)
        val correctOptionId = parseCorrectOptionId(raw, options)

        return QuizQuestion(
            id = questionId,
            prompt = prompt,
            options = options,
            correctOptionId = correctOptionId.ifBlank { options.firstOrNull()?.id ?: "" },
            explanation = explanation,
        )
    }

    /**
     * Parse options from Firestore. Supports two formats:
     * 1. STRING ARRAY (new schema):  ["Option A", "Option B", "Option C"]
     * 2. OBJECT ARRAY (old schema):  [{id, label, answer, ...}, ...]
     * 
     * @return List of QuizOption objects (never empty if valid options exist)
     */
    private fun parseOptions(raw: Map<String, Any?>, questionId: String, questionIndex: Int): List<QuizOption> {
        val optionsList = raw[CourseFirestorePaths.ContentFields.OPTIONS] ?: return emptyList()
        
        return when {
            // NEW SCHEMA: Options are plain strings: ["Option A", "Option B", ...]
            optionsList is List<*> && optionsList.isNotEmpty() && optionsList.first() is String -> {
                optionsList
                    .filterIsInstance<String>()
                    .mapIndexed { optIndex, label ->
                        QuizOption(
                            id = "${questionId}_opt_${optIndex + 1}",  // e.g., "q_123_opt_1"
                            label = label.trim()
                        )
                    }
            }
            
            // OLD SCHEMA: Options are objects: [{id, label, answer, ...}, ...]
            optionsList is List<*> -> {
                listField(raw, CourseFirestorePaths.ContentFields.OPTIONS)
                    .mapIndexedNotNull { optIndex, rawOption ->
                        mapOption(rawOption, questionId, optIndex)
                    }
            }
            
            else -> emptyList()
        }
    }

    /**
     * Determine the correct option ID from Firestore data. Supports two formats:
     * 1. correctOptionIndex (new):  0, 1, 2, 3 (integer index into options array)
     * 2. correct (old):             "option_id_string"
     * 
     * @param raw Raw question data from Firestore
     * @param options Parsed QuizOption list (for index lookup)
     * @return Option ID string (empty if not found, caller uses fallback)
     */
    private fun parseCorrectOptionId(raw: Map<String, Any?>, options: List<QuizOption>): String {
        // NEW SCHEMA: Try correctOptionIndex first (most reliable)
        val correctIndex = (raw["correctOptionIndex"] as? Number)?.toInt()
        if (correctIndex != null && correctIndex >= 0 && correctIndex < options.size) {
            return options[correctIndex].id
        }
        
        // OLD SCHEMA: Try string-based "correct" field
        val correctId = stringField(raw, CourseFirestorePaths.ContentFields.CORRECT)
        if (!correctId.isNullOrBlank()) {
            return correctId
        }
        
        return ""  // Empty = caller will use first option as fallback
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
