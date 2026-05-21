package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.UserProfile
import com.google.firebase.Timestamp

object UserFirestoreMapper {

    fun toUserProfile(documentId: String, data: Map<String, Any>?): UserProfile? {
        if (data == null) return null
        val dto = mapToUserDocument(data)
        val uid = dto.uid?.takeIf { it.isNotBlank() } ?: documentId
        val email = dto.email.orEmpty()
        val displayName = dto.displayName?.takeIf { it.isNotBlank() }
            ?: defaultDisplayName(email, uid)
        return UserProfile(
            uid = uid,
            displayName = displayName,
            email = email,
            avatarUrl = dto.avatarUrl?.takeIf { it.isNotBlank() }
                ?: defaultAvatarUrl(uid),
            phone = dto.phone.orEmpty(),
            streak = dto.streak?.toInt() ?: 0,
            xp = dto.xp?.toInt() ?: 0,
            lastStudyDate = dto.lastStudyDate.orEmpty(),
            joinedAt = dto.joinedAt ?: 0L,
        )
    }

    /** Partial update map for the signed-in user's own document (users/{uid}). */
    fun profileUpdateMap(displayName: String, phone: String): Map<String, Any> = mapOf(
        "displayName" to displayName.trim(),
        "phone" to phone.trim(),
    )

    fun newUserFirestoreMap(
        uid: String,
        email: String,
        displayName: String? = null,
        joinedAt: Long = System.currentTimeMillis(),
    ): Map<String, Any?> = mapOf(
        "uid" to uid,
        "displayName" to (displayName?.takeIf { it.isNotBlank() } ?: defaultDisplayName(email, uid)),
        "email" to email.trim(),
        "avatarUrl" to defaultAvatarUrl(uid),
        "phone" to "",
        "streak" to 0,
        "xp" to 0,
        "lastStudyDate" to "",
        "joinedAt" to joinedAt,
    )

    fun defaultDisplayName(email: String, uid: String): String {
        val localPart = email.substringBefore("@").trim()
        if (localPart.isNotBlank()) return localPart.replaceFirstChar { it.uppercase() }
        return "SmartReview User"
    }

    fun defaultAvatarUrl(uid: String): String =
        "https://picsum.photos/seed/${uid.take(12)}/200/200"

    private fun mapToUserDocument(data: Map<String, Any?>): UserDocument =
        UserDocument(
            uid = stringField(data, "uid", "userId", "user_id"),
            displayName = stringField(data, "displayName", "display_name", "name"),
            email = stringField(data, "email"),
            phone = stringField(data, "phone", "phoneNumber", "phone_number"),
            avatarUrl = stringField(data, "avatarUrl", "avatar_url", "photoUrl", "photo_url"),
            streak = numberField(data, "streak"),
            xp = numberField(data, "xp", "experience", "points"),
            lastStudyDate = stringField(data, "lastStudyDate", "last_study_date"),
            joinedAt = timestampField(data, "joinedAt", "joined_at", "createdAt", "created_at"),
        )

    private fun stringField(data: Map<String, Any?>, vararg keys: String): String? {
        for (key in keys) {
            val value = data[key] as? String
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun numberField(data: Map<String, Any?>, vararg keys: String): Long? {
        for (key in keys) {
            when (val value = data[key]) {
                is Number -> return value.toLong()
            }
        }
        return null
    }

    private fun timestampField(data: Map<String, Any?>, vararg keys: String): Long? {
        for (key in keys) {
            when (val value = data[key]) {
                is Timestamp -> return value.toDate().time
                is Number -> return value.toLong()
            }
        }
        return null
    }
}
