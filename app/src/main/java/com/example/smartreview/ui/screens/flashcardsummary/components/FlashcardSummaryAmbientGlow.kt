package com.example.smartreview.ui.screens.flashcardsummary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.theme.GradientStart
import com.example.smartreview.ui.theme.Secondary

@Composable
internal fun FlashcardSummaryAmbientGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .blur(radius = 90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GradientStart.copy(alpha = 0.20f), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 60.dp)
                .blur(radius = 80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Secondary.copy(alpha = 0.12f), Color.Transparent),
                    ),
                ),
        )
    }
}
