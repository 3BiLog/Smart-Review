package com.example.smartreview.ui.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

// ─── Route constants ──────────────────────────────────────────────────────────

object OnboardingRoutes {
    const val GRAPH        = "onboarding_graph"
    const val FREE_COURSES = "onboarding_free_courses"
    const val STUDY_PLAN   = "onboarding_study_plan"
}

// ─── Nested NavGraph (plugs into the main NavHost) ────────────────────────────

fun NavGraphBuilder.onboardingGraph(
    navController:       NavHostController,
    onOnboardingFinished: () -> Unit,   // navigate to Login / Home
    onSkip:              () -> Unit,    // skip straight to Login
) {
    navigation(
        startDestination = OnboardingRoutes.FREE_COURSES,
        route            = OnboardingRoutes.GRAPH,
    ) {
        composable(OnboardingRoutes.FREE_COURSES) {
            OnboardingFreeCoursesScreen(
                onNext = { navController.navigate(OnboardingRoutes.STUDY_PLAN) },
                onSkip = onSkip,
            )
        }
        composable(OnboardingRoutes.STUDY_PLAN) {
            OnboardingStudyPlanScreen(
                onSignUp  = onOnboardingFinished,
                onLogIn   = onOnboardingFinished,
                onBack    = { navController.popBackStack() },
            )
        }
    }
}