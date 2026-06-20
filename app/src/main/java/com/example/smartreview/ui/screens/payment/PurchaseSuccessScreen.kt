package com.example.smartreview.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.repository.EnrollmentRepository
import com.example.smartreview.data.repository.EnrollmentRepositoryProvider
import com.example.smartreview.ui.screens.coursedetail.courseDetailRoute
import com.example.smartreview.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PurchaseSuccessScreen(
    navController: NavHostController,
    courseId: String,
    justPaid: Boolean = false,
    enrollmentRepository: EnrollmentRepository = EnrollmentRepositoryProvider.default,
) {
    val scope = rememberCoroutineScope()
    val authState by AuthSession.state.collectAsStateWithLifecycle()
    val uid = authState.uid

    Scaffold(
        containerColor = Background,
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Thanh toán thành công!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Khóa học đã được mở khóa.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (uid != null) {
                            enrollmentRepository.isEnrolled(uid, courseId)
                            delay(500)
                        }
                        navController.navigate(courseDetailRoute(courseId, justPaid = true)) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Vào khóa học", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Quay lại")
            }
        }
    }
}