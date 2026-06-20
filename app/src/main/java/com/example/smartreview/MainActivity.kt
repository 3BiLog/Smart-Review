package com.example.smartreview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.learning.StudyTimeManager
import com.example.smartreview.data.repository.LearningProgressRepositoryProvider
import com.example.smartreview.ui.navigation.AppAuthSessionGate
import com.example.smartreview.ui.navigation.SmartReviewNavGraph
import com.example.smartreview.ui.screens.payment.PaymentRoutes
import com.example.smartreview.ui.theme.SmartReviewTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    private var pendingPaymentCourseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StudyTimeManager.init(applicationContext)

        AuthSession.ensureStarted()
        LearningProgressRepositoryProvider.init(applicationContext)

        Log.d(
            "FIREBASE_PROJECT",
            FirebaseApp.getInstance().options.projectId ?: "NULL",
        )

        handlePaymentDeepLink(intent)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                StudyTimeManager.onAppPaused()
                Log.d("MainActivity", "App paused - study time paused")
            }

            override fun onResume(owner: LifecycleOwner) {
                StudyTimeManager.checkAndResetDaily()
                StudyTimeManager.onAppResumed()
                Log.d("MainActivity", "App resumed - study time resumed")
            }
        })

        setContent {
            SmartReviewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val courseId = pendingPaymentCourseId

                    LaunchedEffect(courseId) {
                        if (!courseId.isNullOrBlank()) {
                            navController.navigate(PaymentRoutes.successRoute(courseId)) {
                                launchSingleTop = true
                            }
                            pendingPaymentCourseId = null
                        }
                    }

                    AppAuthSessionGate(navController) {
                        SmartReviewNavGraph(navController)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePaymentDeepLink(intent)
    }

    private fun handlePaymentDeepLink(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (data.scheme != "smartreview" || data.host != "payment") return

        val courseId = data.getQueryParameter("courseId")
        val status = data.getQueryParameter("status")

        if (!courseId.isNullOrBlank() && status == "success") {
            pendingPaymentCourseId = courseId
        }
    }
}