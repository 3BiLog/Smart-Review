package com.example.smartreview.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageOwnershipTest {

    @Test
    fun isMessageFromCurrentUser_matchesUid() {
        assertTrue(isMessageFromCurrentUser("uid_a", "uid_a"))
    }

    @Test
    fun isMessageFromCurrentUser_rejectsOtherUsers() {
        assertFalse(isMessageFromCurrentUser("uid_a", "uid_b"))
    }

    @Test
    fun isMessageFromCurrentUser_requiresAuthenticatedUid() {
        assertFalse(isMessageFromCurrentUser("uid_a", null))
        assertFalse(isMessageFromCurrentUser("uid_a", ""))
    }
}
