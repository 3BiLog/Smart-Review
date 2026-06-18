package com.example.smartreview.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.data.learning.StudyTimeManager
import com.example.smartreview.ui.auth.AuthRoutes
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.screens.profile.components.ChangePasswordDialog
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    navController: NavHostController,
    vm: ProfileViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val studyMinutes by StudyTimeManager.totalStudyMinutes.collectAsState()

    Scaffold(
        containerColor = Background,
        bottomBar = { SmartReviewBottomBar(navController) },
        topBar = {
            ProfileTopBar(
                isEditMode = state.isEditMode,
                onRefresh = { vm.refreshProfile() },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.isAuthenticated) {
                ProfileGuestBanner(
                    onSignIn = {
                        navController.navigate(AuthRoutes.GRAPH) { launchSingleTop = true }
                    },
                )
            }

            state.profileMessage?.let { msg ->
                ProfileMessageBanner(message = msg, onDismiss = vm::dismissMessage)
            }

            ProfileHeroCard(
                avatarUrl = state.avatarUrl,
                displayName = state.displayName,
                levelLabel = state.levelLabel,
                email = if (state.isAuthenticated) state.email else "",
                streak = state.streak.toInt(),
                xp = state.xp.toInt(),
                isLoading = state.isLoadingProfile,
                isAuthenticated = state.isAuthenticated,
                isEditMode = state.isEditMode,
                onEditClick = {
                    if (state.isEditMode) vm.cancelEditMode() else vm.enterEditMode()
                },
            )

            if (state.isEditMode) {
                ProfileEditModeBar(
                    onSave = vm::saveProfile,
                    onCancel = vm::cancelEditMode,
                    isSaving = state.isSavingProfile,
                )
            }

            ProfileSectionCard(title = "Thông tin cá nhân") {
                ProfileFormField(
                    value = state.fullName,
                    label = "Họ và tên",
                    leadingIcon = Icons.Default.Person,
                    onValueChange = vm::onFullNameChange,
                    enabled = state.isEditMode,
                    readOnly = !state.isEditMode,
                )
                ProfileFormField(
                    value = state.email,
                    label = "Email",
                    leadingIcon = Icons.Default.Email,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    keyboardType = KeyboardType.Email,
                )
                ProfileFormField(
                    value = state.phone,
                    label = "Số điện thoại",
                    leadingIcon = Icons.Default.Phone,
                    onValueChange = vm::onPhoneChange,
                    enabled = state.isEditMode,
                    readOnly = !state.isEditMode,
                    keyboardType = KeyboardType.Phone,
                )
                if (!state.isEditMode && state.isAuthenticated) {
                    Text(
                        "Nhấn biểu tượng chỉnh sửa trên avatar để cập nhật thông tin.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                    )
                }
            }

            ProfileSectionCard(title = "Mục tiêu học tập") {
                Text(
                    "Thời gian học mỗi ngày",
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(15, 30, 60).forEach { mins ->
                        val selected = state.dailyGoalMinutes == mins
                        val isCompleted = state.isGoalCompleted && selected
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                    else if (isCompleted) Brush.linearGradient(listOf(Secondary.copy(0.2f), Secondary.copy(0.1f)))
                                    else Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainer)),
                                )
                                .border(
                                    width = if (selected) 0.dp else 1.dp,
                                    color = if (isCompleted && !selected) Secondary.copy(0.4f) else GlassBorder,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable { vm.selectGoal(mins) },
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$mins",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        selected -> Color.White
                                        isCompleted -> Secondary
                                        else -> OnSurface
                                    },
                                )
                                Text(
                                    if (isCompleted && selected) "Hoàn thành!" else "PHÚT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        selected -> Color.White.copy(0.8f)
                                        isCompleted -> Secondary
                                        else -> OnSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }

                // ✅ Show today's progress with real-time data
                if (state.isAuthenticated) {
                    Spacer(Modifier.height(8.dp))
                    val totalMinutes = state.todayStudyTime + studyMinutes.toInt()
                    val goalMinutes = state.dailyGoalMinutes
                    val isCompleted = totalMinutes >= goalMinutes

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Hôm nay: $totalMinutes phút",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                        )
                        if (isCompleted) {
                            Text(
                                "Đã đạt mục tiêu! +${state.dailyGoalXP} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = Secondary,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Text(
                                "Còn ${(goalMinutes - totalMinutes).coerceAtLeast(0)} phút",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary,
                            )
                        }
                    }
                }
            }

            ProfileSectionCard(title = "Tùy chọn") {
                ProfilePreferenceRow(
                    icon = Icons.Default.DarkMode,
                    title = "Giao diện tối",
                    sub = "Theo hệ thống",
                    checked = state.darkModeEnabled,
                    onToggle = vm::toggleDarkMode,
                )
                HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                ProfilePreferenceRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Thông báo đẩy",
                    sub = "Nhắc học & cập nhật",
                    checked = state.notificationsOn,
                    onToggle = vm::toggleNotifications,
                )
            }

            ProfileSectionCard(title = "Bảo mật") {
                OutlinedButton(
                    onClick = { vm.showChangePasswordDialog() },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Primary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.LockReset, null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Đổi mật khẩu", color = Primary)
                }
            }

            Button(
                onClick = {
                    vm.logout {
                        navController.navigate(AuthRoutes.GRAPH) { launchSingleTop = true }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor.copy(0.15f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Logout, null, tint = ErrorColor)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.isAuthenticated) "Đăng xuất" else "Đăng xuất / Về đăng nhập",
                    color = ErrorColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // ✅ Change Password Dialog
    if (state.showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { vm.dismissChangePasswordDialog() },
            onConfirm = { currentPassword, newPassword ->
                vm.changePassword(currentPassword, newPassword)
            },
            isLoading = state.isChangingPassword,
            errorMessage = state.passwordChangeError
        )
    }

    // ✅ Auto-clear success message after delay
    if (state.passwordChangeSuccess) {
        LaunchedEffect(Unit) {
            delay(3000)
            vm.clearPasswordChangeSuccess()
        }
    }
}

@Composable
private fun ProfileTopBar(
    isEditMode: Boolean,
    onRefresh: () -> Unit,
) {
    Surface(color = GlassBg) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Text(
                if (isEditMode) "CHỈNH SỬA HỒ SƠ" else "HỒ SƠ",
                style = MaterialTheme.typography.titleMedium.copy(
                    brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(start = 8.dp),
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Làm mới", tint = Primary)
            }
        }
    }
}