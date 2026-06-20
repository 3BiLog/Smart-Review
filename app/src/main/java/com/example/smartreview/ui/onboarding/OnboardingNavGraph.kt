package com.example.smartreview.ui.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object OnboardingRoutes {
    const val GRAPH        = "onboarding_graph"
    const val FREE_COURSES = "onboarding_free_courses"
    const val STUDY_PLAN   = "onboarding_study_plan"
}


fun NavGraphBuilder.onboardingGraph(
    navController:       NavHostController,
    onOnboardingFinished: () -> Unit,
    onSkip:              () -> Unit,
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