package com.example.smartreview.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.theme.*

@Composable
fun AuthRequiredBanner(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
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
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = onAction) {
                Text(actionLabel, color = Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
