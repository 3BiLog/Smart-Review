package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.model.UserProfile

object LeaderboardFirestoreMapper {

    const val LEADERBOARD_LIMIT = 50L

    fun fromUserProfiles(
        profiles: List<UserProfile>,
        currentUserId: String?,
    ): List<LeaderboardEntry> {
        if (profiles.isEmpty()) return emptyList()

        val ranked = profiles.sortedByDescending { it.xp }
        val maxXp = ranked.first().xp.coerceAtLeast(1)

        return ranked.mapIndexed { index, profile ->
            LeaderboardEntry(
                rank = index + 1,
                userId = profile.uid,
                displayName = profile.displayName,
                avatarUrl = profile.avatarUrl ?: UserFirestoreMapper.defaultAvatarUrl(profile.uid),
                score = profile.xp,
                progress = profile.xp.toFloat() / maxXp,
                isCurrentUser = !currentUserId.isNullOrBlank() && profile.uid == currentUserId,
            )
        }
    }
}
