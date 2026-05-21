package com.example.smartreview.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartreview.ui.theme.*

@Composable
fun ProfileHeroCard(
    avatarUrl: String,
    displayName: String,
    levelLabel: String,
    email: String,
    streak: Int,
    xp: Int,
    isLoading: Boolean,
    isAuthenticated: Boolean,
    isEditMode: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp),
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .border(3.dp, Brush.linearGradient(listOf(GradientStart, GradientEnd)), CircleShape)
                        .padding(3.dp),
                ) {
                    if (isLoading) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(SurfaceContainer),
                        ) {
                            CircularProgressIndicator(
                                color = Primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    } else {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    }
                }
                if (isAuthenticated && !isLoading) {
                    FilledIconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary),
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Hủy chỉnh sửa" else "Chỉnh sửa",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
            )
            Text(levelLabel, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            if (email.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(email, style = MaterialTheme.typography.labelMedium, color = Primary)
            }

            if (isAuthenticated) {
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ProfileStatChip(
                        icon = Icons.Outlined.LocalFireDepartment,
                        label = "Streak",
                        value = "$streak ngày",
                        modifier = Modifier.weight(1f),
                    )
                    ProfileStatChip(
                        icon = Icons.Outlined.Star,
                        label = "XP",
                        value = xp.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStatChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnSurface)
            }
        }
    }
}

@Composable
fun ProfileGuestBanner(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Bạn đang xem hồ sơ mẫu",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                )
                Text(
                    "Đăng nhập để đồng bộ XP, streak và thông tin từ Firestore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                )
            }
            TextButton(onClick = onSignIn) {
                Text("Đăng nhập", color = Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProfileEditModeBar(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text("Hủy", color = OnSurfaceVariant)
            }
            Button(
                onClick = onSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.weight(1f),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(if (isSaving) "Đang lưu…" else "Lưu")
            }
        }
    }
}

@Composable
fun ProfileMessageBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Secondary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            Icon(Icons.Default.Info, null, tint = Secondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider(color = GlassBorder)
            content()
        }
    }
}

@Composable
fun ProfileFormField(
    value: String,
    label: String,
    leadingIcon: ImageVector,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = { Icon(leadingIcon, null, tint = OnSurfaceVariant) },
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = GlassBorder,
            focusedLabelColor = Primary,
            unfocusedLabelColor = OnSurfaceVariant,
            cursorColor = Primary,
            focusedTextColor = OnSurface,
            unfocusedTextColor = OnSurface,
            disabledTextColor = OnSurfaceVariant,
            unfocusedContainerColor = Background,
            focusedContainerColor = Background,
            disabledContainerColor = Background.copy(alpha = 0.7f),
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun ProfilePreferenceRow(
    icon: ImageVector,
    title: String,
    sub: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
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
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedTrackColor = SurfaceVariant,
            ),
        )
    }
}
