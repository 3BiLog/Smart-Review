package com.example.smartreview.data.remote.firestore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserFirestoreMapperTest {

    @Test
    fun toUserProfile_mapsPhoneField() {
        val profile = UserFirestoreMapper.toUserProfile(
            "uid_1",
            mapOf(
                "uid" to "uid_1",
                "email" to "user@example.com",
                "displayName" to "Minh",
                "phone" to "+84 912 345 678",
            ),
        )
        assertNotNull(profile)
        assertEquals("+84 912 345 678", profile!!.phone)
    }

    @Test
    fun profileUpdateMap_containsEditableFieldsOnly() {
        val map = UserFirestoreMapper.profileUpdateMap("Minh Tran", "0901234567")
        assertEquals("Minh Tran", map["displayName"])
        assertEquals("0901234567", map["phone"])
        assertFalse(map.containsKey("email"))
        assertFalse(map.containsKey("uid"))
    }
}
