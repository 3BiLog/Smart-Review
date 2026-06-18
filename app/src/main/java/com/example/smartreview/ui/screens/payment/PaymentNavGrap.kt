package com.example.smartreview.ui.screens.payment

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavGraphBuilder.paymentGraph(navController: NavHostController) {
    composable(
        route = PaymentRoutes.METHOD,
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("courseName") { type = NavType.StringType; defaultValue = "" },
            navArgument("coursePrice") { type = NavType.LongType; defaultValue = 0L },
        ),
    ) { backStack ->
        val courseId = backStack.arguments?.getString("courseId") ?: return@composable
        val courseName = Uri.decode(backStack.arguments?.getString("courseName").orEmpty())
        val coursePrice = backStack.arguments?.getLong("coursePrice") ?: 0L
        PaymentMethodScreen(
            navController = navController,
            courseId = courseId,
            courseName = courseName,
            coursePrice = coursePrice,
        )
    }

    composable(
        route = PaymentRoutes.PIN,
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("amount") { type = NavType.LongType },
        ),
    ) { backStack ->
        val courseId = backStack.arguments?.getString("courseId") ?: return@composable
        val amount = backStack.arguments?.getLong("amount") ?: 0L
        PaymentPinScreen(
            navController = navController,
            courseId = courseId,
            amount = amount,
        )
    }

    composable(
        route = PaymentRoutes.SUCCESS,
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("justPaid") { type = NavType.BoolType; defaultValue = false }
        ),
    ) { backStack ->
        val courseId = backStack.arguments?.getString("courseId") ?: return@composable
        val justPaid = backStack.arguments?.getBoolean("justPaid") ?: false
        PurchaseSuccessScreen(
            navController = navController,
            courseId = courseId,
            justPaid = justPaid,  // ← truyền tham số này
        )
    }
}
