package com.example.smartreview.ui.auth

import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object AuthRoutes {
    const val GRAPH         = "auth_graph"
    const val LOGIN         = "auth_login"
    const val SIGN_UP       = "auth_sign_up"
    const val VERIFY_PHONE  = "auth_verify_phone"
    const val SUCCESS       = "auth_success"
}

fun NavGraphBuilder.authGraph(
    navController:   NavHostController,
    onAuthComplete:  () -> Unit,
) {
    navigation(
        startDestination = AuthRoutes.LOGIN,
        route            = AuthRoutes.GRAPH,
    ) {
        composable(AuthRoutes.LOGIN) {
            val parentEntry = remember(navController.getBackStackEntry(AuthRoutes.GRAPH)) {
                navController.getBackStackEntry(AuthRoutes.GRAPH)
            }
            val authVm: AuthViewModel = viewModel(parentEntry)
            LoginScreen(
                onLoginSuccess   = onAuthComplete,
                onNavigateSignUp = { navController.navigate(AuthRoutes.SIGN_UP) },
                vm               = authVm,
            )
        }
        composable(AuthRoutes.SIGN_UP) {
            val parentEntry = remember(navController.getBackStackEntry(AuthRoutes.GRAPH)) {
                navController.getBackStackEntry(AuthRoutes.GRAPH)
            }
            val authVm: AuthViewModel = viewModel(parentEntry)
            SignUpScreen(
                onNavigateVerify = { navController.navigate(AuthRoutes.VERIFY_PHONE) },
                onNavigateLogin  = { navController.popBackStack() },
                vm               = authVm,
            )
        }
        composable(AuthRoutes.VERIFY_PHONE) {
            VerifyPhoneScreen(
                onVerified = {
                    navController.navigate(AuthRoutes.SUCCESS) {
                        popUpTo(AuthRoutes.VERIFY_PHONE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(AuthRoutes.SUCCESS) {
            AuthSuccessScreen(
                onDone = {
                    onAuthComplete()
                },
            )
        }
    }
}