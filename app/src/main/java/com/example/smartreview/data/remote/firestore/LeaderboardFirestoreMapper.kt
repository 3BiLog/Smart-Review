package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.model.UserProfile

/**
 * Maps Firestore user profiles to ranked leaderboard entries.
 */
object LeaderboardFirestoreMapper {

    const val LEADERBOARD_LIMIT = 50L

    fun fromUserProfiles(
        profiles: List<UserProfile>,
        currentUserId: String?,
    ): List<LeaderboardEntry> {
        if (profiles.isEmpty()) return emptyList()
        val maxXp = profiles.maxOf { it.xp }.coerceAtLeast(1)
        return profiles.mapIndexed { index, profile ->
            LeaderboardEntry(
                rank = index + 1,
                userId = profile.uid,
                displayName = profile.displayName,
                avatarUrl = profile.avatarUrl,
                score = profile.xp,
                progress = profile.xp.toFloat() / maxXp,
                isCurrentUser = !currentUserId.isNullOrBlank() && profile.uid == currentUserId,
            )
        }
    }
}
