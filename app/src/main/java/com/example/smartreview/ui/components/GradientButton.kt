package com.example.smartreview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.theme.GradientEnd
import com.example.smartreview.ui.theme.GradientStart

@Composable
fun GradientButton(
    text:      String,
    modifier:  Modifier = Modifier,
    radius:    Dp       = 16.dp,
    onClick:   () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 24.dp)
    ) {
        Text(
            text       = text,
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 14.sp
        )
    }
}