package com.example.smartreview.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartreview.ui.auth.AuthRoutes
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.theme.*
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    navController: NavHostController,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        bottomBar      = { SmartReviewBottomBar(navController) },
        topBar         = { ProfileTopBar() }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            // ── Avatar ────────────────────────────────────────────────────
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(3.dp, Brush.linearGradient(listOf(GradientStart, GradientEnd)), CircleShape)
                        .padding(3.dp)
                ) {
                    AsyncImage(
                        model              = state.avatarUrl,
                        contentDescription = "Avatar",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Primary)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(state.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(state.levelLabel, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)

            Spacer(Modifier.height(24.dp))

            // ── Personal Information ──────────────────────────────────────
            SectionCard(title = "Personal Information") {
                ProfileTextField(
                    value       = state.fullName,
                    label       = "Full Name",
                    leadingIcon = Icons.Default.Person,
                    onValueChange = { vm.onFullNameChange(it) }
                )
                ProfileTextField(
                    value       = state.email,
                    label       = "Email Address",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    onValueChange = { vm.onEmailChange(it) }
                )
                ProfileTextField(
                    value       = state.phone,
                    label       = "Phone Number",
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    onValueChange = { vm.onPhoneChange(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Learning Goal ─────────────────────────────────────────────
            SectionCard(title = "Learning Goals") {
                Text("Daily Study Target", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(15, 30, 60).forEach { mins ->
                        val selected = state.dailyGoalMinutes == mins
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                    else          Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainer))
                                )
                                .border(
                                    width  = if (selected) 0.dp else 1.dp,
                                    color  = GlassBorder,
                                    shape  = RoundedCornerShape(12.dp)
                                )
                                .clickable { vm.selectGoal(mins) }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$mins",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else OnSurface
                                )
                                Text(
                                    "MIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White.copy(0.8f) else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Preferences ───────────────────────────────────────────────
            SectionCard(title = "Preferences") {
                PreferenceRow(
                    icon    = Icons.Default.DarkMode,
                    title   = "Dark Mode",
                    sub     = "System default",
                    checked = state.darkModeEnabled,
                    onToggle = { vm.toggleDarkMode() }
                )
                HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                PreferenceRow(
                    icon     = Icons.Default.NotificationsActive,
                    title    = "Push Notifications",
                    sub      = "Reminders & updates",
                    checked  = state.notificationsOn,
                    onToggle = { vm.toggleNotifications() }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Security ──────────────────────────────────────────────────
            SectionCard(title = "Security") {
                OutlinedButton(
                    onClick  = {},
                    shape    = RoundedCornerShape(16.dp),
                    border   = BorderStroke(2.dp, Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LockReset, null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Change Password", color = Primary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Logout ────────────────────────────────────────────────────
            Button(
                onClick  = {
                    vm.logout {
                        navController.navigate(AuthRoutes.GRAPH) {
                            launchSingleTop = true
                        }
                    }
                },
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = ErrorColor.copy(0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, null, tint = ErrorColor)
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = ErrorColor, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = SurfaceContainer, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                title.uppercase(),
                style     = MaterialTheme.typography.labelMedium,
                color     = Primary,
                letterSpacing = 1.5.sp
            )
            HorizontalDivider(color = GlassBorder)
            content()
        }
    }
}

@Composable
private fun ProfileTextField(
    value:        String,
    label:        String,
    leadingIcon:  ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        label          = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon    = { Icon(leadingIcon, null, tint = OnSurfaceVariant) },
        singleLine     = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape          = RoundedCornerShape(12.dp),
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Primary,
            unfocusedBorderColor = GlassBorder,
            focusedLabelColor    = Primary,
            unfocusedLabelColor  = OnSurfaceVariant,
            cursorColor          = Primary,
            focusedTextColor     = OnSurface,
            unfocusedTextColor   = OnSurface,
            unfocusedContainerColor = Background,
            focusedContainerColor   = Background,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PreferenceRow(icon: ImageVector, title: String, sub: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier              = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
            ) {
                Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = OnSurface, style = MaterialTheme.typography.bodyMedium)
                Text(sub, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = Primary,
                uncheckedTrackColor = SurfaceVariant
            )
        )
    }
}

@Composable
private fun ProfileTopBar() {
    Surface(color = GlassBg) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "SMART REVIEW",
                style = MaterialTheme.typography.titleMedium.copy(
                    brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold
                )
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, null, tint = Primary)
            }
        }
    }
}