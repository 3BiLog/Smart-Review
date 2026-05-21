package com.example.smartreview.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.learning.LearningProgressCoordinator
import com.example.smartreview.ui.auth.AuthRoutes

/**
 * Observes [AuthSession] and routes unauthenticated users to the auth graph.
 * Does not change NavHost [startDestination] — only reacts at runtime.
 */
@Composable
fun AppAuthSessionGate(
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    AuthSession.ensureStarted()
    val session by AuthSession.state.collectAsStateWithLifecycle()

    LaunchedEffect(session.isAuthenticated) {
        if (!session.isAuthenticated) {
            LearningProgressCoordinator.clearInMemorySessionStores()
        }
        if (!session.isAuthenticated && !navController.isOnAuthFlow()) {
            navController.navigate(AuthRoutes.GRAPH) {
                launchSingleTop = true
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    content()
}

private fun NavHostController.isOnAuthFlow(): Boolean {
    val entry = currentBackStackEntry ?: return false
    return entry.destination.hierarchy.any { destination ->
        destination.route == AuthRoutes.GRAPH ||
            destination.route == AuthRoutes.LOGIN ||
            destination.route == AuthRoutes.SIGN_UP ||
            destination.route == AuthRoutes.SUCCESS
    }
}
