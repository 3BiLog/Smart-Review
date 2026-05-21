package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardFirestoreMapperTest {

    @Test
    fun fromUserProfiles_assignsRankByOrder() {
        val entries = LeaderboardFirestoreMapper.fromUserProfiles(
            listOf(
                UserProfile("u1", "A", "a@x.com", "https://a", xp = 100),
                UserProfile("u2", "B", "b@x.com", "https://b", xp = 50),
            ),
            currentUserId = "u2",
        )
        assertEquals(1, entries[0].rank)
        assertEquals("u1", entries[0].userId)
        assertEquals(2, entries[1].rank)
        assertTrue(entries[1].isCurrentUser)
        assertFalse(entries[0].isCurrentUser)
    }

    @Test
    fun fromUserProfiles_progressRelativeToMaxXp() {
        val entries = LeaderboardFirestoreMapper.fromUserProfiles(
            listOf(
                UserProfile("u1", "A", "a@x.com", "https://a", xp = 200),
                UserProfile("u2", "B", "b@x.com", "https://b", xp = 100),
            ),
            currentUserId = null,
        )
        assertEquals(1f, entries[0].progress, 0.001f)
        assertEquals(0.5f, entries[1].progress, 0.001f)
    }
}
