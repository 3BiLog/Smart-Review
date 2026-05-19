package com.example.smartreview.ui.screens.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.components.SmartReviewBottomBar
import com.example.smartreview.ui.screens.notifications.components.NotificationTabBar
import com.example.smartreview.ui.screens.notifications.components.NotificationTabContent
import com.example.smartreview.ui.screens.notifications.components.NotificationsTopBar
import com.example.smartreview.ui.theme.*

const val NOTIFICATIONS_ROUTE = "notifications"

@Composable
fun NotificationsScreen(
    navController: NavHostController,
    vm: NotificationsViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        topBar = {
            NotificationsTopBar(
                onMarkAllRead = { vm.markAllRead() },
            )
        },
        bottomBar = { SmartReviewBottomBar(navController) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Text(
                "Trung tâm thông báo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
            )

            NotificationTabBar(
                selectedTab = state.selectedTab,
                unreadNotifications = state.unreadNotifications,
                unreadMessages = state.unreadMessages,
                onSelect = { vm.selectTab(it) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    (slideInHorizontally { w ->
                        if (targetState == NotificationTab.MESSAGES) w else -w
                    } + fadeIn()) togetherWith
                        (slideOutHorizontally { w ->
                            if (targetState == NotificationTab.MESSAGES) -w else w
                        } + fadeOut())
                },
                label = "tabContent",
            ) { tab ->
                val list = if (tab == NotificationTab.NOTIFICATIONS) state.notifications
                else state.messages

                NotificationTabContent(
                    tab = tab,
                    list = list,
                    onItemClick = { vm.markItemRead(it.id) },
                )
            }
        }
    }
}
