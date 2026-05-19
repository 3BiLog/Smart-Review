package com.example.smartreview.ui.screens.notifications.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartreview.ui.screens.notifications.NotificationItem
import com.example.smartreview.ui.screens.notifications.NotificationTab
import com.example.smartreview.ui.screens.notifications.components.cards.NotificationCard

@Composable
internal fun NotificationTabContent(
    tab: NotificationTab,
    list: List<NotificationItem>,
    onItemClick: (NotificationItem) -> Unit,
) {
    if (list.isEmpty()) {
        NotificationsEmptyState(
            isMessages = tab == NotificationTab.MESSAGES,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(list, key = { it.id }) { item ->
                NotificationCard(
                    item = item,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}
