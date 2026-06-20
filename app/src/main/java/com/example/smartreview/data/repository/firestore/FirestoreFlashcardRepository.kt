package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.Flashcard
import com.example.smartreview.data.model.FlashcardDeck
import com.example.smartreview.data.repository.FlashcardRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreFlashcardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FlashcardRepository {

    override suspend fun getDeck(lessonId: String): FlashcardDeck? = withContext(Dispatchers.IO) {
        if (lessonId.isBlank()) return@withContext null

        android.util.Log.d("FirestoreFlashcardRepository", "Looking for flashcard deck: $lessonId")

        try {
            val coursesSnapshot = firestore.collection("courses")
                .whereEqualTo("status", "published")
                .get()
                .await()

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
                        if (type != "flashcard") continue

                        android.util.Log.d("FirestoreFlashcardRepository", "Found flashcard deck: $lessonId")

                        val content = lessonMap["content"] as? Map<*, *>
                        val data = content?.get("data") as? Map<*, *>
                        val cardsList = data?.get("cards") as? List<*> ?: emptyList<Any?>()

                        val cards = ArrayList<Flashcard>()
                        for (cardItem in cardsList) {
                            val cardMap = cardItem as? Map<*, *>
                            if (cardMap != null) {
                                val flashcard = Flashcard(
                                    id = cardMap["id"] as? String ?: "",
                                    front = cardMap["front"] as? String ?: "",
                                    back = cardMap["back"] as? String ?: "",
                                    hint = cardMap["hint"] as? String ?: ""
                                )
                                cards.add(flashcard)
                            }
                        }

                        if (cards.isEmpty()) {
                            android.util.Log.d("FirestoreFlashcardRepository", "No cards found")
                            return@withContext null
                        }

                        android.util.Log.d("FirestoreFlashcardRepository", "Loaded ${cards.size} flashcards")

                        return@withContext FlashcardDeck(
                            id = lessonId,
                            title = lessonMap["title"] as? String ?: "Flashcards",
                            description = "",
                            cards = cards,
                            xpReward = (lessonMap["xpReward"] as? Long) ?: 30,
                            courseId = courseDoc.id,
                            moduleId = moduleMap["id"] as? String,
                            lessonId = lessonId
                        )
                    }
                }
            }
            android.util.Log.d("FirestoreFlashcardRepository", "Deck not found: $lessonId")
            null
        } catch (e: Exception) {
            android.util.Log.e("FirestoreFlashcardRepository", "Error loading deck", e)
            null
        }
    }

    override suspend fun getDefaultDeck(): FlashcardDeck {
        // Try to find any flashcard deck in Firestore
        val coursesSnapshot = firestore.collection("courses")
            .whereEqualTo("status", "published")
            .limit(1)
            .get()
            .await()

        for (courseDoc in coursesSnapshot.documents) {
            val modules = courseDoc.get("modules") as? List<*> ?: continue
            for (module in modules) {
                val moduleMap = module as? Map<*, *> ?: continue
                val lessons = moduleMap["lessons"] as? List<*> ?: continue
                for (lesson in lessons) {
                    val lessonMap = lesson as? Map<*, *> ?: continue
                    if (lessonMap["type"] == "flashcard") {
                        val lessonId = lessonMap["id"] as? String ?: continue
                        return getDeck(lessonId) ?: createEmptyDeck()
                    }
                }
            }
        }
        return createEmptyDeck()
    }

    private fun createEmptyDeck(): FlashcardDeck = FlashcardDeck(
        id = "empty",
        title = "No Flashcards Available",
        cards = listOf()
    )
}