package com.example.smartreview.ui.screens.payment

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation

fun NavGraphBuilder.paymentGraph(navController: NavHostController) {

    // ── PaymentMethodScreen ───────────────────────────────────────────────
    composable(
        route     = PaymentRoutes.METHOD,
        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
    ) { backStack ->
        val courseId = backStack.arguments?.getString("courseId") ?: return@composable
        PaymentMethodScreen(
            navController = navController,
            courseId      = courseId,
        )
    }

    // ── PaymentPinScreen ──────────────────────────────────────────────────
    composable(
        route     = PaymentRoutes.PIN,
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("amount")   { type = NavType.LongType },
        )
    ) { backStack ->
        val courseId = backStack.arguments?.getString("courseId") ?: return@composable
        val amount   = backStack.arguments?.getLong("amount")    ?: 0L
        PaymentPinScreen(
            navController = navController,
            courseId      = courseId,
            amount        = amount,
        )
    }

    // ── PurchaseSuccessScreen ─────────────────────────────────────────────
    composable(
        route     = PaymentRoutes.SUCCESS,
        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
    ) { backStack ->
        val courseId = backStack.arguments?.getString("courseId") ?: return@composable
        PurchaseSuccessScreen(
            navController = navController,
            courseId      = courseId,
        )
    }
}