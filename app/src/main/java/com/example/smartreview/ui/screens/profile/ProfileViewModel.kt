package com.example.smartreview.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.AuthResult
import com.example.smartreview.data.model.UserProfile
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val avatarUrl: String = "https://picsum.photos/seed/profile/200/200",
    val displayName: String = "Alex Mercer",
    val levelLabel: String = "Level 42 Learner",
    val fullName: String = "Alex Mercer",
    val email: String = "alex.mercer@example.com",
    val phone: String = "",
    val dailyGoalMinutes: Int = 30,  // 15, 30, 60
    val todayStudyTime: Int = 0,     // Minutes studied today
    val dailyGoalXP: Int = 0,        // XP earned from daily goal
    val isGoalCompleted: Boolean = false,
    val darkModeEnabled: Boolean = true,
    val notificationsOn: Boolean = true,
    val streak: Long = 0,
    val xp: Long = 0,
    val isLoadingProfile: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isEditMode: Boolean = false,
    val profileMessage: String? = null,
    /** Snapshot for cancel — populated when profile loads from Firestore. */
    val savedFullName: String = "",
    val savedPhone: String = "",

    // Change password states
    val showChangePasswordDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val passwordChangeError: String? = null,
    val passwordChangeSuccess: Boolean = false,
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.default,
    private val userRepository: UserRepository = UserRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        AuthSession.ensureStarted()
        viewModelScope.launch {
            AuthSession.state.collect { session ->
                _uiState.update { it.copy(isAuthenticated = session.isAuthenticated) }
                if (session.isAuthenticated) loadUserProfile()
                else resetToGuestDefaults()
            }
        }
        observeProfileRealtime()
    }

    private fun observeProfileRealtime() {
        viewModelScope.launch {
            AuthSession.state
                .map { it.isAuthenticated }
                .distinctUntilChanged()
                .flatMapLatest { authenticated ->
                    if (authenticated) userRepository.observeCurrentUserProfile()
                    else flowOf(null)
                }
                .collect { profile ->
                    if (profile == null) return@collect
                    _uiState.update { state ->
                        if (state.isEditMode || state.isSavingProfile) state
                        else state.applyUserProfile(profile)
                    }
                }
        }
    }

    fun refreshProfile() = loadUserProfile()

    fun enterEditMode() {
        if (!_uiState.value.isAuthenticated) {
            showMessage("Đăng nhập để chỉnh sửa hồ sơ.")
            return
        }
        _uiState.update { it.copy(isEditMode = true, profileMessage = null) }
    }

    fun cancelEditMode() {
        _uiState.update {
            it.copy(
                isEditMode = false,
                fullName = it.savedFullName,
                phone = it.savedPhone,
                profileMessage = null,
            )
        }
    }

    fun saveProfile() {
        if (!_uiState.value.isAuthenticated) {
            showMessage("Đăng nhập để lưu hồ sơ.")
            return
        }
        val fullName = _uiState.value.fullName.trim()
        val phone = _uiState.value.phone.trim()
        if (fullName.isBlank()) {
            showMessage("Họ và tên không được để trống.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, profileMessage = null) }
            val saved = userRepository.updateCurrentUserProfile(fullName, phone)
            _uiState.update { state ->
                if (saved) {
                    state.copy(
                        isEditMode = false,
                        isSavingProfile = false,
                        savedFullName = fullName,
                        savedPhone = phone,
                        displayName = fullName,
                        profileMessage = "Đã lưu hồ sơ.",
                    )
                } else {
                    state.copy(
                        isSavingProfile = false,
                        profileMessage = "Không lưu được hồ sơ. Kiểm tra đăng nhập hoặc kết nối.",
                    )
                }
            }
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(profileMessage = null) }

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v) }
    fun toggleDarkMode() = _uiState.update { it.copy(darkModeEnabled = !it.darkModeEnabled) }
    fun toggleNotifications() = _uiState.update { it.copy(notificationsOn = !it.notificationsOn) }

    fun logout(onLoggedOut: () -> Unit = {}) {
        authRepository.signOut()
        AuthSession.refresh()
        resetToGuestDefaults()
        onLoggedOut()
    }

    // Change Password Functions
    fun showChangePasswordDialog() {
        _uiState.update {
            it.copy(
                showChangePasswordDialog = true,
                passwordChangeError = null,
                passwordChangeSuccess = false
            )
        }
    }

    fun dismissChangePasswordDialog() {
        _uiState.update {
            it.copy(
                showChangePasswordDialog = false,
                isChangingPassword = false,
                passwordChangeError = null
            )
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, passwordChangeError = null) }

            val result = authRepository.updatePassword(currentPassword, newPassword)

            when (result) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showChangePasswordDialog = false,
                            isChangingPassword = false,
                            passwordChangeSuccess = true,
                            profileMessage = "Đổi mật khẩu thành công! Vui lòng đăng nhập lại."
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            passwordChangeError = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearPasswordChangeSuccess() {
        _uiState.update { it.copy(passwordChangeSuccess = false) }
    }

    // ✅ NEW: Select daily goal with Firebase sync
    fun selectGoal(minutes: Int) {
        _uiState.update { it.copy(dailyGoalMinutes = minutes) }
        // Sync to Firestore
        viewModelScope.launch {
            userRepository.updateDailyGoal(minutes.toLong())
            showMessage("Đã cập nhật mục tiêu: $minutes phút/ngày")
        }
    }

    // ✅ NEW: Add study time (called from Pomodoro or lesson completion)
    fun addStudyTime(minutes: Int) {
        viewModelScope.launch {
            val success = userRepository.addStudyTime(minutes.toLong())
            if (success) {
                // Refresh profile to update UI
                loadUserProfile()
                // Check if goal was completed
                val state = _uiState.value
                if (state.isGoalCompleted) {
                    showMessage("🎉 Chúc mừng! Bạn đã hoàn thành mục tiêu ${state.dailyGoalMinutes} phút hôm nay! +${state.dailyGoalXP} XP")
                }
            }
        }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(profileMessage = message) }
    }

    private fun loadUserProfile() {
        if (!AuthSession.state.value.isAuthenticated) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true, profileMessage = null) }
            val profile = userRepository.getCurrentUserProfile()
            _uiState.update { state ->
                if (profile != null) state.applyUserProfile(profile)
                else state.copy(isLoadingProfile = false, isEditMode = false)
            }
        }
    }

    private fun resetToGuestDefaults() {
        _uiState.value = ProfileUiState(isAuthenticated = false)
    }

    // ✅ FIXED: Apply user profile with correct variable names
    private fun ProfileUiState.applyUserProfile(profile: UserProfile): ProfileUiState {
        val goalMinutes = profile.dailyGoal.toInt()
        val todayMinutes = profile.todayStudyTime.toInt()
        val goalCompleted = todayMinutes >= goalMinutes

        return copy(
            avatarUrl = profile.avatarUrl ?: this.avatarUrl,
            displayName = profile.displayName,
            levelLabel = levelLabelFromXp(profile.xp),
            fullName = profile.displayName,
            email = profile.email,
            phone = profile.phone,
            streak = profile.streak,
            xp = profile.xp,
            dailyGoalMinutes = goalMinutes,
            todayStudyTime = todayMinutes,
            dailyGoalXP = profile.dailyGoalXP.toInt(),
            isGoalCompleted = goalCompleted,
            isLoadingProfile = false,
            isSavingProfile = false,
            isEditMode = false,
            savedFullName = profile.displayName,
            savedPhone = profile.phone,
            isAuthenticated = true,
        )
    }

    private fun levelLabelFromXp(xp: Long): String {
        val level = (xp / 100).toInt().coerceAtLeast(1)
        return "Cấp $level · Người học"
    }
}