package com.example.smartreview.data.learning

import com.example.smartreview.data.model.FlashcardProgressSnapshot
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.model.QuizAnswerRecord
import com.example.smartreview.data.model.QuizProgressSnapshot
import com.example.smartreview.data.model.UserLearningProgress
import org.json.JSONArray
import org.json.JSONObject

object LearningProgressJsonCodec {

    fun encode(progress: UserLearningProgress): String {
        val root = JSONObject()
        root.put("uid", progress.uid)
        root.put("lastUpdatedAt", progress.lastUpdatedAt)
        root.put("completedLessonIds", JSONArray(progress.completedLessonIds.toList()))
        root.put("completedQuizIds", JSONArray(progress.completedQuizIds.toList()))
        progress.flashcardInProgress?.let { root.put("flashcardInProgress", encodeFlashcard(it)) }
        progress.lessonInProgress?.let { root.put("lessonInProgress", encodeLesson(it)) }
        progress.quizInProgress?.let { root.put("quizInProgress", encodeQuiz(it)) }
        return root.toString()
    }

    fun decode(json: String): UserLearningProgress? = runCatching {
        val root = JSONObject(json)
        UserLearningProgress(
            uid = root.getString("uid"),
            lastUpdatedAt = root.optLong("lastUpdatedAt", System.currentTimeMillis()),
            completedLessonIds = root.optJSONArray("completedLessonIds").toStringSet(),
            completedQuizIds = root.optJSONArray("completedQuizIds").toStringSet(),
            flashcardInProgress = root.optJSONObject("flashcardInProgress")?.let { decodeFlashcard(it) },
            lessonInProgress = root.optJSONObject("lessonInProgress")?.let { decodeLesson(it) },
            quizInProgress = root.optJSONObject("quizInProgress")?.let { decodeQuiz(it) },
        )
    }.getOrNull()

    private fun encodeFlashcard(snapshot: FlashcardProgressSnapshot): JSONObject =
        JSONObject().apply {
            put("deckId", snapshot.deckId)
            put("sessionId", snapshot.sessionId)
            put("sessionStartedAt", snapshot.sessionStartedAt)
            put("currentIndex", snapshot.currentIndex)
            put("knownCount", snapshot.knownCount)
            put("reviewCount", snapshot.reviewCount)
            val statuses = JSONObject()
            snapshot.cardStatuses.forEach { (id, status) -> statuses.put(id, status) }
            put("cardStatuses", statuses)
        }

    private fun decodeFlashcard(obj: JSONObject): FlashcardProgressSnapshot {
        val statusesObj = obj.optJSONObject("cardStatuses") ?: JSONObject()
        val statuses = mutableMapOf<String, String>()
        statusesObj.keys().forEach { key -> statuses[key] = statusesObj.getString(key) }
        return FlashcardProgressSnapshot(
            deckId = obj.getString("deckId"),
            sessionId = obj.getString("sessionId"),
            sessionStartedAt = obj.getLong("sessionStartedAt"),
            currentIndex = obj.getInt("currentIndex"),
            cardStatuses = statuses,
            knownCount = obj.optInt("knownCount", 0),
            reviewCount = obj.optInt("reviewCount", 0),
        )
    }

    private fun encodeLesson(snapshot: LessonProgressSnapshot): JSONObject =
        JSONObject().apply {
            put("lessonId", snapshot.lessonId)
            put("sessionId", snapshot.sessionId)
            put("sessionStartedAt", snapshot.sessionStartedAt)
            put("currentBlockIndex", snapshot.currentBlockIndex)
            put("viewedBlockIds", JSONArray(snapshot.viewedBlockIds.toList()))
        }

    private fun decodeLesson(obj: JSONObject): LessonProgressSnapshot =
        LessonProgressSnapshot(
            lessonId = obj.getString("lessonId"),
            sessionId = obj.getString("sessionId"),
            sessionStartedAt = obj.getLong("sessionStartedAt"),
            currentBlockIndex = obj.getInt("currentBlockIndex"),
            viewedBlockIds = obj.optJSONArray("viewedBlockIds").toStringSet(),
        )

    // FIXED: Use selectedOptionId (String) to match QuizProgressSnapshot in LearningProgressModels
    private fun encodeQuiz(snapshot: QuizProgressSnapshot): JSONObject =
        JSONObject().apply {
            put("quizId", snapshot.quizId)
            put("sessionId", snapshot.sessionId)
            put("sessionStartedAt", snapshot.sessionStartedAt)
            put("currentIndex", snapshot.currentIndex)
            put("selectedOptionId", snapshot.selectedOptionId)
            put("showFeedback", snapshot.showFeedback)
            val answers = JSONArray()
            snapshot.answers.forEach { record ->
                answers.put(
                    JSONObject().apply {
                        put("questionId", record.questionId)
                        put("selectedOptionId", record.selectedOptionId)
                        put("isCorrect", record.isCorrect)
                    },
                )
            }
            put("answers", answers)
        }

    // FIXED: Use selectedOptionId (String) to match QuizProgressSnapshot in LearningProgressModels
    private fun decodeQuiz(obj: JSONObject): QuizProgressSnapshot {
        val answersArray = obj.optJSONArray("answers") ?: JSONArray()
        val answers = buildList {
            for (i in 0 until answersArray.length()) {
                val item = answersArray.getJSONObject(i)
                add(
                    QuizAnswerRecord(
                        questionId = item.getString("questionId"),
                        selectedOptionId = item.getString("selectedOptionId"),
                        isCorrect = item.getBoolean("isCorrect"),
                    ),
                )
            }
        }
        return QuizProgressSnapshot(
            quizId = obj.getString("quizId"),
            sessionId = obj.getString("sessionId"),
            sessionStartedAt = obj.getLong("sessionStartedAt"),
            currentIndex = obj.getInt("currentIndex"),
            answers = answers,
            selectedOptionId = obj.optString("selectedOptionId").takeIf { it.isNotBlank() },
            showFeedback = obj.optBoolean("showFeedback", false),
        )
    }

    private fun JSONArray?.toStringSet(): Set<String> {
        if (this == null) return emptySet()
        return buildSet {
            for (i in 0 until length()) add(getString(i))
        }
    }
}