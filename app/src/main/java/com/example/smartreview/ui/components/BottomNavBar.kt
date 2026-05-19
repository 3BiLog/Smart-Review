package com.example.smartreview.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartreview.ui.navigation.Screen
import com.example.smartreview.ui.navigation.navigateSingleTop
import com.example.smartreview.ui.theme.GlassBg
import com.example.smartreview.ui.theme.Primary

data class BottomNavItem(
    val label:       String,
    val route:       String,
    val selectedIcon: ImageVector,
    val icon:        ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home",      Screen.Home.route,      Icons.Filled.Home,    Icons.Outlined.Home),
    BottomNavItem("Courses",   Screen.Courses.route,   Icons.Filled.School,  Icons.Outlined.School),
    BottomNavItem("Search",    Screen.Search.route,    Icons.Filled.Search,  Icons.Outlined.Search),
    BottomNavItem("Community", Screen.Community.route, Icons.Filled.Group,   Icons.Outlined.Group),
    BottomNavItem("Profile",   Screen.Profile.route,   Icons.Filled.Person,  Icons.Outlined.Person),
)

@Composable
fun SmartReviewBottomBar(navController: NavHostController) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    NavigationBar(containerColor = GlassBg, tonalElevation = 0.dp) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { navController.navigateSingleTop(item.route) },
                icon     = {
                    Icon(
                        imageVector  = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label
                    )
                },
                label  = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Primary,
                    selectedTextColor   = Primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}