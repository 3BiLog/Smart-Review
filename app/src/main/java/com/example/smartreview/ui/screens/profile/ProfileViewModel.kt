package com.example.smartreview.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onFullNameChange(v: String)  = _uiState.update { it.copy(fullName = v) }
    fun onEmailChange(v: String)     = _uiState.update { it.copy(email = v) }
    fun onPhoneChange(v: String)     = _uiState.update { it.copy(phone = v) }
    fun selectGoal(minutes: Int)     = _uiState.update { it.copy(dailyGoalMinutes = minutes) }
    fun toggleDarkMode()             = _uiState.update { it.copy(darkModeEnabled = !it.darkModeEnabled) }
    fun toggleNotifications()        = _uiState.update { it.copy(notificationsOn = !it.notificationsOn) }

    fun logout() {
        authRepository.signOut()
    }
}