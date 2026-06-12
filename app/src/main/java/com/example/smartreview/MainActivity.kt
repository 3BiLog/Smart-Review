package com.example.smartreview

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthSession.ensureStarted()
        LearningProgressRepositoryProvider.init(applicationContext)
        Log.d(
            "FIREBASE_PROJECT",
            FirebaseApp.getInstance().options.projectId ?: "NULL"
        )
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