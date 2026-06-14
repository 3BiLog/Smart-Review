package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.ReadingLesson
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreReadingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getReadingLesson(lessonId: String): ReadingLesson? = withContext(Dispatchers.IO) {
        if (lessonId.isBlank()) return@withContext null

        android.util.Log.d("ReadingRepository", "Looking for reading lesson: $lessonId")

        try {
            val coursesSnapshot = firestore.collection("courses")
                .whereEqualTo("status", "published")
                .get()
                .await()

            android.util.Log.d("ReadingRepository", "Checking ${coursesSnapshot.documents.size} courses")

            for (courseDoc in coursesSnapshot.documents) {
                val modules = courseDoc.get("modules") as? List<*> ?: continue

                for (module in modules) {
                    val moduleMap = module as? Map<*, *> ?: continue
                    val lessons = moduleMap["lessons"] as? List<*> ?: continue

                    for (lesson in lessons) {
                        val lessonMap = lesson as? Map<*, *> ?: continue
                        val id = lessonMap["id"] as? String ?: continue

                        if (id != lessonId) continue

                        val type = lessonMap["type"] as? String ?: ""
                        if (type != "reading") continue

                        android.util.Log.d("ReadingRepository", "Found reading lesson: $lessonId in course ${courseDoc.id}")

                        val content = lessonMap["content"] as? Map<*, *>
                        val data = content?.get("data") as? Map<*, *>
                        val markdown = data?.get("markdown") as? String
                            ?: data?.get("text") as? String
                            ?: ""

                        return@withContext ReadingLesson(
                            id = lessonId,
                            title = lessonMap["title"] as? String ?: "Bài đọc",
                            duration = (lessonMap["duration"] as? Long) ?: 0,
                            isFree = lessonMap["isFree"] as? Boolean ?: false,
                            xpReward = (lessonMap["xpReward"] as? Long) ?: 50,
                            order = (lessonMap["order"] as? Long) ?: 0,
                            markdown = markdown,
                            courseId = courseDoc.id,
                            moduleId = moduleMap["id"] as? String
                        )
                    }
                }
            }
            android.util.Log.d("ReadingRepository", "Reading lesson not found: $lessonId")
            null
        } catch (e: Exception) {
            android.util.Log.e("ReadingRepository", "Error loading reading", e)
            null
        }
    }
}