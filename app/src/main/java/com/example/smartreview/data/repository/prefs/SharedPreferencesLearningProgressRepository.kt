package com.example.smartreview.data.repository.prefs

import android.content.Context
import com.example.smartreview.data.learning.LearningProgressJsonCodec
import com.example.smartreview.data.model.UserLearningProgress
import com.example.smartreview.data.repository.LearningProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPreferencesLearningProgressRepository(
    context: Context,
) : LearningProgressRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun load(uid: String): UserLearningProgress? = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext null
        val json = prefs.getString(prefKey(uid), null) ?: return@withContext null
        LearningProgressJsonCodec.decode(json)?.takeIf { it.uid == uid }
    }

    override suspend fun save(progress: UserLearningProgress) = withContext(Dispatchers.IO) {
        if (progress.uid.isBlank()) return@withContext
        prefs.edit()
            .putString(prefKey(progress.uid), LearningProgressJsonCodec.encode(progress))
            .apply()
    }

    override suspend fun clear(uid: String) = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext
        prefs.edit().remove(prefKey(uid)).apply()
    }

    private fun prefKey(uid: String) = "learning_progress_$uid"

    companion object {
        private const val PREFS_NAME = "smart_review_learning_progress"
    }
}
