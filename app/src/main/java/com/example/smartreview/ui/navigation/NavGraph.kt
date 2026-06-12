package com.example.smartreview.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartreview.ui.screens.flashcard.FlashcardScreen
import com.example.smartreview.ui.screens.home.HomeScreen
import com.example.smartreview.ui.screens.pomodoro.PomodoroScreen
import com.example.smartreview.ui.screens.profile.ProfileScreen
import com.example.smartreview.ui.screens.flashcardsummary.FLASHCARD_SUMMARY_ROUTE
import com.example.smartreview.ui.screens.flashcardsummary.FlashcardSummaryScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.smartreview.ui.screens.courses.CourseListScreen
import com.example.smartreview.ui.screens.courses.COURSES_LIST_ROUTE
import com.example.smartreview.ui.screens.coursedetail.CourseDetailScreen
import com.example.smartreview.ui.screens.coursedetail.COURSE_DETAIL_ROUTE
import com.example.smartreview.ui.screens.lesson.LessonScreen
import com.example.smartreview.ui.screens.lesson.LESSON_ROUTE
import com.example.smartreview.ui.screens.lessonplayer.LessonVideoPlayerScreen
import com.example.smartreview.ui.screens.lessonplayer.LESSON_PLAYER_ROUTE
import com.example.smartreview.ui.screens.lessonsummary.LESSON_SUMMARY_ROUTE
import com.example.smartreview.ui.screens.lessonsummary.LessonSummaryScreen
import com.example.smartreview.ui.screens.quiz.QUIZ_ROUTE
import com.example.smartreview.ui.screens.quiz.QuizScreen
import com.example.smartreview.ui.screens.quizsummary.QUIZ_SUMMARY_ROUTE
import com.example.smartreview.ui.screens.quizsummary.QuizSummaryScreen
import com.example.smartreview.ui.screens.search.SEARCH_ROUTE
import com.example.smartreview.ui.screens.search.SearchScreen
import com.example.smartreview.ui.screens.community.CommunityRoomsScreen
import com.example.smartreview.ui.screens.community.COMMUNITY_ROOMS_ROUTE
import com.example.smartreview.ui.screens.community.CHAT_ROOM_ROUTE
import com.example.smartreview.ui.screens.chatroom.ChatRoomScreen
import com.example.smartreview.ui.screens.leaderboard.LeaderboardScreen
import com.example.smartreview.ui.screens.leaderboard.LEADERBOARD_ROUTE
import com.example.smartreview.ui.screens.notifications.NotificationsScreen
import com.example.smartreview.ui.screens.notifications.NOTIFICATIONS_ROUTE
import com.example.smartreview.ui.auth.AuthRoutes
import com.example.smartreview.ui.auth.authGraph
import com.example.smartreview.ui.onboarding.OnboardingRoutes
import com.example.smartreview.ui.onboarding.onboardingGraph
import com.example.smartreview.ui.navigation.RouteHelpers

sealed class Screen(val route: String) {
    object Home      : Screen("home")
    object Courses   : Screen(COURSES_LIST_ROUTE)
    object Search    : Screen(SEARCH_ROUTE)
    object Community : Screen(COMMUNITY_ROOMS_ROUTE)
    object Profile   : Screen("profile")
    object Flashcard : Screen("flashcard")
    object Pomodoro  : Screen("pomodoro")
}

@Composable
fun SmartReviewNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route,
    ) {
        onboardingGraph(
            navController        = navController,
            onOnboardingFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(OnboardingRoutes.GRAPH) { inclusive = true }
                }
            },
            onSkip = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(OnboardingRoutes.GRAPH) { inclusive = true }
                }
            },
        )
        authGraph(
            navController  = navController,
            onAuthComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(AuthRoutes.GRAPH) { inclusive = true }
                }
            },
        )
        composable(Screen.Home.route)      { HomeScreen(navController) }
        composable(Screen.Flashcard.route) { FlashcardScreen(navController) }
        composable(Screen.Pomodoro.route)  { PomodoroScreen(navController) }
        composable(Screen.Profile.route)   { ProfileScreen(navController) }

        // TEMPORARILY COMMENTED - Flashcard summary may still work
        composable(
            route = FLASHCARD_SUMMARY_ROUTE,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            FlashcardSummaryScreen(
                navController = navController,
                sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty(),
            )
        }

        composable(COURSES_LIST_ROUTE) {
             CourseListScreen(navController = navController)
         }
         composable(
             route     = COURSE_DETAIL_ROUTE,
             arguments = listOf(navArgument("courseId") { type = NavType.StringType })
         ) { backStackEntry ->
             CourseDetailScreen(
                 navController = navController,
                 courseId      = backStackEntry.arguments?.getString("courseId") ?: "",
             )
         }
          composable(
             route = RouteHelpers.LESSON_PLAYER_ROUTE,
             arguments = listOf(
                 navArgument("courseId") { type = NavType.StringType },
                 navArgument("lessonId") { type = NavType.StringType },
             ),
         ) { backStackEntry ->
             LessonVideoPlayerScreen(
                 navController = navController,
                 lessonId      = backStackEntry.arguments?.getString("lessonId") ?: "",
                 courseId      = backStackEntry.arguments?.getString("courseId"),
             )
         }
         // Legacy single-arg route (backwards compatibility)
         composable(
             route     = LESSON_PLAYER_ROUTE,
             arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
         ) { backStackEntry ->
             LessonVideoPlayerScreen(
                 navController = navController,
                 lessonId      = backStackEntry.arguments?.getString("lessonId") ?: "",
             )
         }
         composable(
             route = LESSON_ROUTE,
             arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
         ) { backStackEntry ->
             LessonScreen(
                 navController = navController,
                 lessonId = backStackEntry.arguments?.getString("lessonId").orEmpty(),
             )
         }
         composable(
             route = LESSON_SUMMARY_ROUTE,
             arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
         ) { backStackEntry ->
             LessonSummaryScreen(
                 navController = navController,
                 sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty(),
             )
         }
         composable(
             route = QUIZ_ROUTE,
             arguments = listOf(navArgument("quizId") { type = NavType.StringType }),
         ) { backStackEntry ->
             QuizScreen(
                 navController = navController,
                 quizId = backStackEntry.arguments?.getString("quizId").orEmpty(),
             )
         }
         composable(
             route = QUIZ_SUMMARY_ROUTE,
             arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
         ) { backStackEntry ->
             QuizSummaryScreen(
                 navController = navController,
                 sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty(),
             )
         }
         composable(SEARCH_ROUTE) {
             SearchScreen(navController = navController)
         }

        composable(COMMUNITY_ROOMS_ROUTE) {
            CommunityRoomsScreen(navController = navController)
        }

        composable(
            route     = CHAT_ROOM_ROUTE,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            ChatRoomScreen(
                navController = navController,
                roomId        = backStackEntry.arguments?.getString("roomId") ?: "r1",
            )
        }

        composable(LEADERBOARD_ROUTE) {
            LeaderboardScreen(navController = navController)
        }
        composable(NOTIFICATIONS_ROUTE) {
            NotificationsScreen(navController = navController)
        }
    }
}

fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }
}