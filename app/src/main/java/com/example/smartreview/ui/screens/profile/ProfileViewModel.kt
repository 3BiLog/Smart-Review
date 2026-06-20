package com.example.smartreview.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.learning.StudyTimeManager
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
    val dailyGoalMinutes: Int = 30,
    val todayStudyTimeBase: Int = 0,
    val dailyGoalXP: Int = 0,
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
    val savedFullName: String = "",
    val savedPhone: String = "",
    val showChangePasswordDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val passwordChangeError: String? = null,
    val passwordChangeSuccess: Boolean = false,
) {
    val todayStudyTime: Int
        get() = todayStudyTimeBase + StudyTimeManager.totalStudyMinutes.value.toInt()
}

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
                if (session.isAuthenticated) {
                    loadUserProfile()
                } else {
                    resetToGuestDefaults()
                }
            }
        }
        observeProfileRealtime()
        observeStudyTime()
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

    private fun observeStudyTime() {
        viewModelScope.launch {
            StudyTimeManager.totalStudyMinutes.collect { _ ->
                // Chỉ cần cập nhật state để recompute todayStudyTime (computed property)
                _uiState.update { it.copy() }
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

    fun selectGoal(minutes: Int) {
        _uiState.update { it.copy(dailyGoalMinutes = minutes) }
        viewModelScope.launch {
            userRepository.updateDailyGoal(minutes.toLong())
            showMessage("Đã cập nhật mục tiêu: $minutes phút/ngày")
        }
    }

    fun addStudyTime(minutes: Int) {
        viewModelScope.launch {
            val success = userRepository.addStudyTime(minutes.toLong())
            if (success) {
                // Refresh profile để cập nhật todayStudyTimeBase
                loadUserProfile()
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
                if (profile != null) {
                    checkAndResetStudyTimeManager(profile)
                    state.applyUserProfile(profile)
                } else {
                    state.copy(isLoadingProfile = false, isEditMode = false)
                }
            }
        }
    }

    private fun checkAndResetStudyTimeManager(profile: UserProfile) {
        val lastReset = profile.lastResetDate?.toDate()
        val today = java.util.Calendar.getInstance()
        if (lastReset == null || !isSameDay(today, lastReset)) {
            StudyTimeManager.forceResetDaily()
            android.util.Log.d("ProfileViewModel", "StudyTimeManager reset due to new day")
        }
    }

    private fun isSameDay(cal1: java.util.Calendar, date2: java.util.Date): Boolean {
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun resetToGuestDefaults() {
        _uiState.value = ProfileUiState(isAuthenticated = false)
    }

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
            todayStudyTimeBase = todayMinutes,
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