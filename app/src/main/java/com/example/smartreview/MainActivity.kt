package com.example.smartreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.repository.LearningProgressRepositoryProvider
import com.example.smartreview.ui.navigation.AppAuthSessionGate
import com.example.smartreview.ui.navigation.SmartReviewNavGraph
import com.example.smartreview.ui.theme.SmartReviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthSession.ensureStarted()
        LearningProgressRepositoryProvider.init(applicationContext)
        setContent {
            SmartReviewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppAuthSessionGate(navController) {
                        SmartReviewNavGraph(navController)
                    }
                }
            }
        }
    }
}

// // Trong MainActivity.kt, kiểm tra nếu onboarding đã hoàn thành:
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val prefs    = getSharedPreferences("smart_review", MODE_PRIVATE)
//        val hasSeenOnboarding = prefs.getBoolean("onboarding_done", false)
//
//        setContent {
//            SmartReviewTheme {
//                val navController = rememberNavController()
//
//                // ── Decide start destination ─────────────────────────────────
//                val startRoute = if (hasSeenOnboarding) Screen.Home.route
//                                 else OnboardingRoutes.GRAPH
//
//                NavHost(
//                    navController    = navController,
//                    startDestination = startRoute,
//                ) {
//                    // Onboarding nested graph
//                    onboardingGraph(
//                        navController        = navController,
//                        onOnboardingFinished = {
//                            prefs.edit().putBoolean("onboarding_done", true).apply()
//                            navController.navigate(Screen.Home.route) {
//                                popUpTo(OnboardingRoutes.GRAPH) { inclusive = true }
//                            }
//                        },
//                        onSkip = {
//                            prefs.edit().putBoolean("onboarding_done", true).apply()
//                            navController.navigate(Screen.Home.route) {
//                                popUpTo(OnboardingRoutes.GRAPH) { inclusive = true }
//                            }
//                        },
//                    )
//
//                    // All other app routes
//                    composable(Screen.Home.route)      { HomeScreen(navController) }
//                    composable(Screen.Profile.route)   { ProfileScreen(navController) }
//                    // ... other routes
//                }
//            }
//        }
//    }
//}