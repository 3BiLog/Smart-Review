package com.example.smartreview.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smartreview.ui.navigation.Screen
import com.example.smartreview.ui.theme.*

@Composable
fun PurchaseSuccessScreen(
    navController: NavHostController,
    courseId:      String,
) {
    Scaffold(
        containerColor = Background,
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint     = Secondary,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Thanh toán thành công!",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = OnSurface,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Khóa học đã được mở khóa.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = OnSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick  = {
                    navController.popBackStack(Screen.Home.route, false)
                },
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Về trang chủ", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
