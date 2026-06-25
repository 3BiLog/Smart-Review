package com.example.smartreview.ui.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.theme.*

@Composable
fun AuthBrandSection(
    icon:     ImageVector = Icons.Default.Psychology,
    subtitle: String      = "Nâng cao hiệu suất học tập của bạn",
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.radialGradient(listOf(Primary.copy(0.30f), Color.Transparent)),
                        CircleShape,
                    )
            )
            Surface(
                color  = SurfaceContainer,
                shape  = CircleShape,
                modifier = Modifier
                    .size(72.dp)
                    .border(2.dp, Primary.copy(0.40f), CircleShape),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = Primary,
                        modifier           = Modifier.size(38.dp),
                    )
                }
            }
        }

        Text(
            text  = "SMART REVIEW",
            style = MaterialTheme.typography.headlineLarge.copy(
                brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                fontWeight = FontWeight.Bold,
            ),
        )

        Text(
            text      = subtitle,
            style     = MaterialTheme.typography.bodyMedium,
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun AuthTextField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    leadingIcon:   ImageVector,
    modifier:      Modifier         = Modifier,
    keyboardType:  KeyboardType     = KeyboardType.Text,
    imeAction:     ImeAction        = ImeAction.Next,
    onImeAction:   () -> Unit       = {},
    enabled:       Boolean          = true,
    isError:       Boolean          = false,
    supportingText: String?        = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        OutlinedTextField(
            value          = value,
            onValueChange  = onValueChange,
            enabled        = enabled,
            isError        = isError,
            leadingIcon    = { Icon(leadingIcon, null, tint = OnSurfaceVariant) },
            singleLine     = true,
            shape          = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext   = { onImeAction() },
                onDone   = { onImeAction() },
                onSearch = { onImeAction() },
            ),
            supportingText = supportingText?.let {
                {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isError) ErrorColor else OnSurfaceVariant,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = if (isError) ErrorColor else Primary,
                errorBorderColor        = ErrorColor,
                unfocusedContainerColor = SurfaceContainer,
                focusedContainerColor   = SurfaceContainer,
                disabledContainerColor  = SurfaceContainer.copy(alpha = 0.6f),
                unfocusedTextColor      = OnSurface,
                focusedTextColor        = OnSurface,
                cursorColor             = Primary,
                unfocusedPlaceholderColor = OnSurfaceVariant.copy(0.5f),
                focusedPlaceholderColor   = OnSurfaceVariant.copy(0.5f),
                unfocusedLeadingIconColor = OnSurfaceVariant,
                focusedLeadingIconColor   = Primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AuthPasswordField(
    value:            String,
    onValueChange:    (String) -> Unit,
    label:            String,
    isVisible:        Boolean,
    onToggleVisibility: () -> Unit,
    modifier:         Modifier   = Modifier,
    imeAction:        ImeAction  = ImeAction.Done,
    onImeAction:      () -> Unit = {},
    enabled:          Boolean    = true,
    isError:          Boolean    = false,
    supportingText:   String?    = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        OutlinedTextField(
            value               = value,
            onValueChange       = onValueChange,
            enabled             = enabled,
            isError             = isError,
            leadingIcon         = { Icon(Icons.Default.Lock, null, tint = OnSurfaceVariant) },
            trailingIcon        = {
                IconButton(onClick = onToggleVisibility, enabled = enabled) {
                    Icon(
                        if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null,
                        tint = OnSurfaceVariant,
                    )
                }
            },
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine           = true,
            shape                = RoundedCornerShape(12.dp),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
            keyboardActions      = KeyboardActions(onDone = { onImeAction() }),
            supportingText = supportingText?.let {
                {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isError) ErrorColor else OnSurfaceVariant,
                    )
                }
            },
            colors               = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = if (isError) ErrorColor else Primary,
                errorBorderColor        = ErrorColor,
                unfocusedContainerColor = SurfaceContainer,
                focusedContainerColor   = SurfaceContainer,
                disabledContainerColor  = SurfaceContainer.copy(alpha = 0.6f),
                unfocusedTextColor      = OnSurface,
                focusedTextColor        = OnSurface,
                cursorColor             = Primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun GradientAuthButton(
    text:      String,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier,
    enabled:   Boolean  = true,
    isLoading: Boolean  = false,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                else          Brush.linearGradient(listOf(OnSurfaceVariant.copy(0.3f), OnSurfaceVariant.copy(0.3f)))
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun AuthDivider(label: String = "Hoặc tiếp tục với", modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = modifier,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = OnSurfaceVariant.copy(0.25f))
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = OnSurfaceVariant.copy(0.25f))
    }
}

@Composable
fun SocialLoginButton(
    label:   String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(2.dp, SurfaceVariant),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainer),
        modifier = modifier.height(52.dp),
    ) {
        Surface(
            color    = when (label) {
                "Google" -> Color(0xFFDE5246).copy(0.20f)
                "Apple"  -> OnSurface.copy(0.15f)
                else     -> Primary.copy(0.15f)
            },
            shape    = CircleShape,
            modifier = Modifier.size(24.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text       = label.first().toString(),
                    color      = when (label) {
                        "Google" -> Color(0xFFDE5246)
                        else     -> OnSurface
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 12.sp,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = OnSurface, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun BoxScope.AuthBackgroundOrbs() {
    Box(
        modifier = Modifier
            .size(350.dp)
            .align(Alignment.TopStart)
            .offset(x = (-100).dp, y = (-100).dp)
            .background(Brush.radialGradient(listOf(GradientStart.copy(0.18f), Color.Transparent)), CircleShape)
    )
    Box(
        modifier = Modifier
            .size(350.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 100.dp, y = 100.dp)
            .background(Brush.radialGradient(listOf(Secondary.copy(0.10f), Color.Transparent)), CircleShape)
    )
}

@Composable
fun AuthFormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = GlassBg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
            content = content,
        )
    }
}

@Composable
fun AuthErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        color = ErrorColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(Icons.Default.ErrorOutline, null, tint = ErrorColor, modifier = Modifier.size(18.dp))
            Text(message, color = ErrorColor, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun AuthScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun AuthTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(color = GlassBg, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Primary)
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
fun AuthValidationHint(
    text: String,
    isValid: Boolean? = null,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = when (isValid) {
        true  -> Icons.Default.CheckCircle to Secondary
        false -> Icons.Default.Cancel to ErrorColor
        null  -> Icons.Default.Info to OnSurfaceVariant
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@Composable
fun AuthPasswordStrengthBar(password: String, modifier: Modifier = Modifier) {
    val strength = when {
        password.length >= 10 -> 3
        password.length >= 8  -> 2
        password.length >= AuthUiState.MIN_PASSWORD_LENGTH -> 1
        else -> 0
    }
    val label = when (strength) {
        3 -> "Mạnh"
        2 -> "Khá"
        1 -> "Đủ dùng"
        else -> "Quá ngắn"
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < strength) {
                                when (strength) {
                                    3 -> Secondary
                                    2 -> Primary
                                    else -> OnSurfaceVariant
                                }
                            } else {
                                SurfaceVariant.copy(alpha = 0.5f)
                            },
                        ),
                )
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
fun AuthSignUpChecklist(
    fullNameOk: Boolean,
    emailOk: Boolean,
    passwordOk: Boolean,
    passwordsMatch: Boolean,
    termsOk: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AuthValidationHint("Họ tên hợp lệ", isValid = fullNameOk)
        AuthValidationHint("Email hợp lệ", isValid = emailOk)
        AuthValidationHint("Mật khẩu đủ ${AuthUiState.MIN_PASSWORD_LENGTH} ký tự", isValid = passwordOk)
        AuthValidationHint("Mật khẩu xác nhận khớp", isValid = passwordsMatch)
        AuthValidationHint("Đồng ý điều khoản", isValid = termsOk)
    }
}

@Composable
fun AuthLoadingOverlay(isLoading: Boolean, message: String = "Đang xử lý...") {
    if (!isLoading) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = SurfaceContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
            }
        }
    }
}

@Composable
fun AuthFooterText(
    prefix:   String,
    linkText: String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier              = modifier,
    ) {
        Text("$prefix ", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(
            text       = linkText,
            style      = MaterialTheme.typography.labelLarge,
            color      = Primary,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.clickable(onClick = onClick),
        )
    }
}