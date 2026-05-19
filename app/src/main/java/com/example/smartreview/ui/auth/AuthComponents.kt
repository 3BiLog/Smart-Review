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

// ─────────────────────────────────────────────────────────────────────────────
// BRAND SECTION  (logo + title + subtitle)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthBrandSection(
    icon:     ImageVector = Icons.Default.Psychology,
    subtitle: String      = "Nâng cao hiệu suất học tập của bạn",
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Icon circle with glow
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

        // Brand name
        Text(
            text  = "SMART REVIEW",
            style = MaterialTheme.typography.headlineLarge.copy(
                brush      = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                fontWeight = FontWeight.Bold,
            ),
        )

        // Subtitle
        Text(
            text      = subtitle,
            style     = MaterialTheme.typography.bodyMedium,
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GLASS TEXT FIELD
// ─────────────────────────────────────────────────────────────────────────────
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
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        OutlinedTextField(
            value          = value,
            onValueChange  = onValueChange,
            leadingIcon    = { Icon(leadingIcon, null, tint = OnSurfaceVariant) },
            singleLine     = true,
            shape          = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext   = { onImeAction() },
                onDone   = { onImeAction() },
                onSearch = { onImeAction() },
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Primary,
                unfocusedContainerColor = SurfaceContainer,
                focusedContainerColor   = SurfaceContainer,
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

// ─────────────────────────────────────────────────────────────────────────────
// PASSWORD TEXT FIELD (with visibility toggle)
// ─────────────────────────────────────────────────────────────────────────────
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
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        OutlinedTextField(
            value               = value,
            onValueChange       = onValueChange,
            leadingIcon         = { Icon(Icons.Default.Lock, null, tint = OnSurfaceVariant) },
            trailingIcon        = {
                IconButton(onClick = onToggleVisibility) {
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
            colors               = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Primary,
                unfocusedContainerColor = SurfaceContainer,
                focusedContainerColor   = SurfaceContainer,
                unfocusedTextColor      = OnSurface,
                focusedTextColor        = OnSurface,
                cursorColor             = Primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GRADIENT AUTH BUTTON
// ─────────────────────────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// DIVIDER  "Hoặc tiếp tục với"
// ─────────────────────────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// SOCIAL BUTTON  (Google / Apple)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SocialLoginButton(
    label:   String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick  = onClick,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(2.dp, SurfaceVariant),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainer),
        modifier = modifier.height(52.dp),
    ) {
        // Social "logo" – colored initial
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

// ─────────────────────────────────────────────────────────────────────────────
// BACKGROUND ORBS  (decorative, no blur required)
// ─────────────────────────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// AUTH FOOTER LINK
// ─────────────────────────────────────────────────────────────────────────────
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