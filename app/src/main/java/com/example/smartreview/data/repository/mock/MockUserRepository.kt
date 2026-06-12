//package com.example.smartreview.data.repository.mock
//
//import com.example.smartreview.data.mock.MockUserData
//import com.example.smartreview.data.model.UserProfile
//import com.example.smartreview.data.repository.UserRepository
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//
///**
// * Local mock profiles for offline/debug and Firestore fallback.
// */
//class MockUserRepository : UserRepository {
//
//    private val profileState = MutableStateFlow(MockUserData.defaultProfile)
//
//    override suspend fun getCurrentUserProfile(): UserProfile = profileState.value
//
//    override fun observeCurrentUserProfile(): Flow<UserProfile?> = profileState.asStateFlow()
//
//    override suspend fun getUserProfile(uid: String): UserProfile? =
//        profileState.value.takeIf { it.uid == uid }
//
//    override suspend fun updateCurrentUserProfile(
//        displayName: String,
//        phone: String,
//    ): Boolean {
//        val trimmedName = displayName.trim()
//        if (trimmedName.isBlank()) return false
//        profileState.value = profileState.value.copy(
//            displayName = trimmedName,
//            phone = phone.trim(),
//        )
//        return true
//    }
//
//    override suspend fun ensureUserProfileExists(
//        uid: String,
//        email: String,
//        displayName: String?,
//    ): UserProfile {
//        val profile = MockUserData.defaultProfile.copy(
//            uid = uid,
//            email = email,
//            displayName = displayName?.takeIf { it.isNotBlank() } ?: MockUserData.defaultProfile.displayName,
//        )
//        profileState.value = profile
//        return profile
//    }
//
//    fun applyGamificationUpdate(newXp: Int, newStreak: Int, lastStudyDate: String) {
//        profileState.value = profileState.value.copy(
//            xp = newXp,
//            streak = newStreak,
//            lastStudyDate = lastStudyDate,
//        )
//    }
//}
