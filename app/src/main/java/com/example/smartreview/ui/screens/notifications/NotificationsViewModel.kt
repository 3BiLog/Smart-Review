package com.example.smartreview.ui.screens.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


enum class NotificationType {
    CHALLENGE,
    COURSE_UPDATE,
    PROMOTIONAL,
    MESSAGE,
    SYSTEM,
    COMMUNITY,
}

enum class NotificationTab(val label: String) {
    NOTIFICATIONS("Thông báo"),
    MESSAGES("Tin nhắn"),
}

data class NotificationItem(
    val id:              String,
    val type:            NotificationType,
    val title:           String,
    val content:         String,
    val time:            String,
    val isRead:          Boolean = true,
    // CHALLENGE
    val progress:        Float?  = null,
    // MESSAGE
    val avatarUrl:       String? = null,
    val isAvatarOnline:  Boolean = false,
    val senderName:      String? = null,
    // PROMOTIONAL
    val imageUrl:        String? = null,
    val badgeLabel:      String? = null,
)

data class NotificationsUiState(
    val selectedTab:   NotificationTab         = NotificationTab.NOTIFICATIONS,
    val notifications: List<NotificationItem>  = emptyList(),
    val messages:      List<NotificationItem>  = emptyList(),
    val unreadNotifications: Int               = 0,
    val unreadMessages:      Int               = 0,
)

class NotificationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { loadMockData() }

    fun selectTab(tab: NotificationTab) =
        _uiState.update { it.copy(selectedTab = tab) }

    fun markAllRead() {
        _uiState.update { state ->
            when (state.selectedTab) {
                NotificationTab.NOTIFICATIONS ->
                    state.copy(
                        notifications = state.notifications.map { it.copy(isRead = true) },
                        unreadNotifications = 0,
                    )
                NotificationTab.MESSAGES ->
                    state.copy(
                        messages = state.messages.map { it.copy(isRead = true) },
                        unreadMessages = 0,
                    )
            }
        }
    }

    fun markItemRead(id: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                },
                messages = state.messages.map {
                    if (it.id == id) it.copy(isRead = true) else it
                },
                unreadNotifications = state.notifications.count { !it.isRead },
                unreadMessages      = state.messages.count { !it.isRead },
            )
        }
    }

    private fun loadMockData() {
        val notifications = listOf(
            NotificationItem(
                id       = "n1",
                type     = NotificationType.CHALLENGE,
                title    = "Thử thách mới!",
                content  = "Bạn vừa mở khóa thử thách 'Master of UX'. Hoàn thành trong 24h để nhận 500 XP bonus!",
                time     = "2 phút trước",
                isRead   = false,
                progress = 0.75f,
            ),
            NotificationItem(
                id      = "n2",
                type    = NotificationType.COURSE_UPDATE,
                title   = "Cập nhật khóa học",
                content = "Giảng viên vừa thêm 3 bài giảng mới vào 'Advanced JavaScript 2024'. Xem ngay!",
                time    = "1 giờ trước",
                isRead  = false,
            ),
            NotificationItem(
                id         = "n3",
                type       = NotificationType.PROMOTIONAL,
                title      = "Tuần lễ Gamification",
                content    = "Tham gia cộng đồng để nhận huy hiệu giới hạn.",
                time       = "3 giờ trước",
                isRead     = true,
                imageUrl   = "https://picsum.photos/seed/promo1/600/300",
                badgeLabel = "SỰ KIỆN",
            ),
            NotificationItem(
                id      = "n4",
                type    = NotificationType.SYSTEM,
                title   = "Bảo trì hệ thống",
                content = "Hệ thống sẽ bảo trì định kỳ vào lúc 02:00 AM chủ nhật này. Vui lòng lưu tiến trình học tập trước đó.",
                time    = "Hôm qua",
                isRead  = true,
            ),
            NotificationItem(
                id      = "n5",
                type    = NotificationType.COMMUNITY,
                title   = "Nhóm học tập",
                content = "Có 12 bài thảo luận mới trong nhóm 'Hành trình UI/UX 2024'. Tham gia thảo luận ngay!",
                time    = "Hôm qua",
                isRead  = true,
            ),
            NotificationItem(
                id      = "n6",
                type    = NotificationType.COURSE_UPDATE,
                title   = "Huy hiệu mới",
                content = "Bạn đã nhận được huy hiệu 'Streak Master – 7 ngày liên tiếp' 🔥",
                time    = "2 ngày trước",
                isRead  = true,
            ),
        )

        val messages = listOf(
            NotificationItem(
                id              = "m1",
                type            = NotificationType.MESSAGE,
                title           = "Linh Chi",
                senderName      = "Linh Chi",
                content         = "\"Này, bạn đã xem bài giải Lab 3 chưa? Khó thật đấy...\"",
                time            = "4 giờ trước",
                isRead          = false,
                avatarUrl       = "https://picsum.photos/seed/linh/80/80",
                isAvatarOnline  = true,
            ),
            NotificationItem(
                id             = "m2",
                type           = NotificationType.MESSAGE,
                title          = "Minh Tuấn",
                senderName     = "Minh Tuấn",
                content        = "Mọi người ơi câu 45 test 2 chọn gì vậy?",
                time           = "5 giờ trước",
                isRead         = true,
                avatarUrl      = "https://picsum.photos/seed/minh/80/80",
                isAvatarOnline = true,
            ),
            NotificationItem(
                id             = "m3",
                type           = NotificationType.MESSAGE,
                title          = "Sarah Drasner",
                senderName     = "Sarah Drasner",
                content        = "Great work on today's assignment! Keep it up 🎉",
                time           = "Hôm qua",
                isRead         = true,
                avatarUrl      = "https://picsum.photos/seed/sarah/80/80",
                isAvatarOnline = false,
            ),
            NotificationItem(
                id             = "m4",
                type           = NotificationType.MESSAGE,
                title          = "Lan Anh",
                senderName     = "Lan Anh",
                content        = "Let's discuss about environmental issues for IELTS tomorrow.",
                time           = "Hôm qua",
                isRead         = true,
                avatarUrl      = "https://picsum.photos/seed/lan/80/80",
                isAvatarOnline = false,
            ),
        )

        _uiState.update {
            it.copy(
                notifications       = notifications,
                messages            = messages,
                unreadNotifications = notifications.count { n -> !n.isRead },
                unreadMessages      = messages.count { m -> !m.isRead },
            )
        }
    }
}