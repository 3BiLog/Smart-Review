package com.example.smartreview.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Color Palette (từ design) ───────────────────────────────────────────────
val Primary          = Color(0xFF9D4EDD)
val PrimaryVariant   = Color(0xFF7B2CBF)
val Secondary        = Color(0xFF00E5FF)
val SecondaryDim     = Color(0xFF45F1C5)
val Tertiary         = Color(0xFFFFB785)
val Background       = Color(0xFF1C1B1F)
val Surface          = Color(0xFF2B2930)
val SurfaceVariant   = Color(0xFF353438)
val SurfaceContainer = Color(0xFF201F23)
val OnSurface        = Color(0xFFE5E1E7)
val OnSurfaceVariant = Color(0xFFC7C4D8)
val GlassBorder      = Color(0x0DFFFFFF)
val GlassBg          = Color(0x991A1A2E)
val ErrorColor       = Color(0xFFFFB4AB)
val GradientStart    = Color(0xFF6C63FF)
val GradientEnd      = Color(0xFF9B59B6)

private val DarkColorScheme = darkColorScheme(
    primary          = Primary,
    secondary        = Secondary,
    tertiary         = Tertiary,
    background       = Background,
    surface          = Surface,
    surfaceVariant   = SurfaceVariant,
    onPrimary        = Color.White,
    onSecondary      = Color(0xFF003828),
    onBackground     = OnSurface,
    onSurface        = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error            = ErrorColor,
)

@Composable
fun SmartReviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}
