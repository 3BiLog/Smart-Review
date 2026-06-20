package com.example.smartreview.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.smartreview.ui.navigation.FORGOT_PASSWORD_ROUTE

object AuthRoutes {
    const val GRAPH = "auth_graph"
    const val LOGIN = "auth_login"
    const val SIGN_UP = "auth_sign_up"
    const val SUCCESS = "auth_success"
}

@Composable
private fun rememberAuthViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
): AuthViewModel {
    val owner = remember(backStackEntry) {
        navController.safeAuthGraphViewModelOwner(backStackEntry)
    }
    return viewModel(owner)
}

private fun NavHostController.safeAuthGraphViewModelOwner(
    backStackEntry: NavBackStackEntry,
): NavBackStackEntry {
    val parent = backStackEntry.destination.parent
        ?: return backStackEntry
    return try {
        getBackStackEntry(parent.id)
    } catch (_: IllegalArgumentException) {
        backStackEntry
    }
}

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthComplete: () -> Unit,
) {
    navigation(
        startDestination = AuthRoutes.LOGIN,
        route = AuthRoutes.GRAPH,
    ) {
        composable(AuthRoutes.LOGIN) { backStackEntry ->
            val authVm = rememberAuthViewModel(navController, backStackEntry)
            LoginScreen(
                onLoginSuccess = onAuthComplete,
                onNavigateSignUp = { navController.navigate(AuthRoutes.SIGN_UP) },
                navController = navController,
                vm = authVm,
            )
        }

        composable(AuthRoutes.SIGN_UP) { backStackEntry ->
            val authVm = rememberAuthViewModel(navController, backStackEntry)
            SignUpScreen(
                onRegisterSuccess = {
                    navController.navigate(AuthRoutes.SUCCESS) {
                        popUpTo(AuthRoutes.SIGN_UP) { inclusive = true }
                    }
                },
                onNavigateLogin = { navController.popBackStack() },
                vm = authVm,
            )
        }

        composable(AuthRoutes.SUCCESS) { backStackEntry ->
            val authVm = rememberAuthViewModel(navController, backStackEntry)
            AuthSuccessScreen(vm = authVm, onDone = onAuthComplete)
        }

        composable(FORGOT_PASSWORD_ROUTE) { backStackEntry ->
            val forgotVm: ForgotPasswordViewModel = viewModel(backStackEntry)
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetSent = {
                    navController.popBackStack()
                },
                vm = forgotVm
            )
        }
    }
}