package com.example.smartreview.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.UserProfile
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val avatarUrl:         String  = "https://picsum.photos/seed/profile/200/200",
    val displayName:       String  = "Alex Mercer",
    val levelLabel:        String  = "Level 42 Learner",
    val fullName:          String  = "Alex Mercer",
    val email:             String  = "alex.mercer@example.com",
    val phone:             String  = "+1 (555) 019-2834",
    val dailyGoalMinutes:  Int     = 30,   // 15 | 30 | 60
    val darkModeEnabled:   Boolean = true,
    val notificationsOn:   Boolean = true,
    val streak:            Int     = 0,
    val xp:                Int     = 0,
    val isLoadingProfile:  Boolean = false,
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.default,
    private val userRepository: UserRepository = UserRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        AuthSession.ensureStarted()
        loadUserProfile()
        viewModelScope.launch {
            AuthSession.state.collect { session ->
                if (session.isAuthenticated) loadUserProfile()
                else resetToGuestDefaults()
            }
        }
    }

    fun refreshProfile() = loadUserProfile()

    fun onFullNameChange(v: String)  = _uiState.update { it.copy(fullName = v) }
    fun onEmailChange(v: String)     = _uiState.update { it.copy(email = v) }
    fun onPhoneChange(v: String)     = _uiState.update { it.copy(phone = v) }
    fun selectGoal(minutes: Int)     = _uiState.update { it.copy(dailyGoalMinutes = minutes) }
    fun toggleDarkMode()             = _uiState.update { it.copy(darkModeEnabled = !it.darkModeEnabled) }
    fun toggleNotifications()        = _uiState.update { it.copy(notificationsOn = !it.notificationsOn) }

    fun logout(onLoggedOut: () -> Unit = {}) {
        authRepository.signOut()
        AuthSession.refresh()
        resetToGuestDefaults()
        onLoggedOut()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true) }
            val profile = if (authRepository.isAuthenticated()) {
                userRepository.getCurrentUserProfile()
            } else {
                null
            }
            _uiState.update { state ->
                if (profile != null) state.applyUserProfile(profile)
                else state.copy(isLoadingProfile = false)
            }
        }
    }

    private fun resetToGuestDefaults() {
        _uiState.value = ProfileUiState()
    }

    private fun ProfileUiState.applyUserProfile(profile: UserProfile): ProfileUiState = copy(
        avatarUrl = profile.avatarUrl,
        displayName = profile.displayName,
        levelLabel = levelLabelFromXp(profile.xp),
        fullName = profile.displayName,
        email = profile.email,
        streak = profile.streak,
        xp = profile.xp,
        isLoadingProfile = false,
    )

    private fun levelLabelFromXp(xp: Int): String {
        val level = (xp / 100).coerceAtLeast(1)
        return "Level $level Learner"
    }
}
