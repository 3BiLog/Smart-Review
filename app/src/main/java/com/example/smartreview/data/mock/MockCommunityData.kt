package com.example.smartreview.data.mock

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
import com.example.smartreview.data.model.MessageType
import com.example.smartreview.data.model.RoomIconType

object MockCommunityData {

    private val memberAvatars = List(5) { "https://picsum.photos/seed/av$it/50/50" }

    val rooms = listOf(
        ChatRoom(
            id = "r1",
            name = "Luyện thi TOEIC 800+",
            lastMessage = "Minh Tuấn: Mọi người ơi câu 45 test 2 chọn gì vậy?",
            lastMessageTime = "Vừa xong",
            isOnline = true,
            unreadCount = 3,
            memberCount = 15,
            iconType = RoomIconType.SCHOOL,
            memberAvatars = memberAvatars.take(3),
        ),
        ChatRoom(
            id = "r2",
            name = "Góc học tập & Chém gió",
            lastMessage = "Bạn: Hôm nay cày xong 2 part rồi nghỉ thôi anh em.",
            lastMessageTime = "10:45 AM",
            isOnline = false,
            unreadCount = 0,
            memberCount = 47,
            iconType = RoomIconType.FORUM,
            memberAvatars = memberAvatars.take(2),
            isCurrentUserLast = true,
        ),
        ChatRoom(
            id = "r3",
            name = "Lớp Ôn Thi Đại Học 2024",
            lastMessage = "Trần Nam: Cần thêm tài liệu về đạo hàm...",
            lastMessageTime = "10:45 AM",
            isOnline = true,
            unreadCount = 3,
            memberCount = 14,
            iconType = RoomIconType.SCHOOL,
            memberAvatars = memberAvatars.take(3),
        ),
        ChatRoom(
            id = "r4",
            name = "Fullstack Developers VN",
            lastMessage = "Minh Lê: Tailwind v4 có gì mới không anh em?",
            lastMessageTime = "09:12 AM",
            isOnline = true,
            unreadCount = 0,
            memberCount = 47,
            iconType = RoomIconType.CODE,
            memberAvatars = memberAvatars.take(2),
        ),
        ChatRoom(
            id = "r5",
            name = "IELTS Speaking Practice",
            lastMessage = "Lan Anh: Let's discuss about environmental issues.",
            lastMessageTime = "Hôm qua",
            isOnline = false,
            unreadCount = 12,
            memberCount = 10,
            iconType = RoomIconType.LANGUAGE,
            memberAvatars = memberAvatars.take(2),
        ),
        ChatRoom(
            id = "r6",
            name = "Thảo luận Kỹ năng mềm",
            lastMessage = "Hoàng: Cách để quản lý thời gian hiệu quả?",
            lastMessageTime = "Hôm qua",
            isOnline = true,
            unreadCount = 0,
            memberCount = 23,
            iconType = RoomIconType.PSYCHOLOGY,
            memberAvatars = memberAvatars.take(2),
        ),
        ChatRoom(
            id = "r7",
            name = "Thông báo hệ thống",
            lastMessage = "Tính năng chấm điểm Writing AI mới đã cập nhật!",
            lastMessageTime = "Hôm qua",
            isOnline = true,
            unreadCount = 0,
            memberCount = 999,
            iconType = RoomIconType.ANNOUNCEMENT,
            isSystemRoom = true,
        ),
    )

    val suggestedRooms = listOf(
        ChatRoom(
            id = "s1",
            name = "Cộng đồng Thiết kế UI/UX",
            lastMessage = "3,400 thành viên đang trực tuyến",
            lastMessageTime = "",
            isOnline = true,
            memberCount = 3400,
            iconType = RoomIconType.GLOBE,
        ),
        ChatRoom(
            id = "s2",
            name = "Data Science Vietnam",
            lastMessage = "12 nhóm mới",
            lastMessageTime = "",
            isOnline = true,
            memberCount = 800,
            iconType = RoomIconType.CODE,
        ),
    )

    fun roomNameFor(roomId: String): String = when (roomId) {
        "r1" -> "Luyện thi TOEIC 800+"
        "r2" -> "Góc học tập & Chém gió"
        else -> "Community Chat"
    }

    val defaultMessages = listOf(
        ChatMessage("d1", "", "", "", "TODAY", "", MessageType.DATE_SEPARATOR),
        ChatMessage(
            id = "m1",
            senderId = "u1",
            senderName = "Alex Rivera",
            senderAvatar = "https://picsum.photos/seed/alex/50/50",
            content = "Has anyone started the \"Advanced Neural Patterns\" module? The first quiz is surprisingly tricky! 🧠",
            time = "10:24 AM",
            type = MessageType.TEXT,
        ),
        ChatMessage(
            id = "m2",
            senderId = "me",
            senderName = "Bạn",
            senderAvatar = "https://picsum.photos/seed/me/50/50",
            content = "Just finished it! Pro tip: pay close attention to the visual cues in the micro-animations. They literally give away the answers. ⚡️",
            time = "10:26 AM",
            type = MessageType.TEXT,
            isCurrentUser = true,
        ),
        ChatMessage(
            id = "m3",
            senderId = "u2",
            senderName = "Sarah Chen",
            senderAvatar = "https://picsum.photos/seed/sarah/50/50",
            content = "Check out this visualization I generated from the course data. It really clarifies the hierarchy! 📊",
            time = "10:30 AM",
            type = MessageType.IMAGE,
            imageUrl = "https://picsum.photos/seed/chart/400/200",
        ),
        ChatMessage(
            id = "m4",
            senderId = "u1",
            senderName = "Alex Rivera",
            senderAvatar = "https://picsum.photos/seed/alex/50/50",
            content = "That's amazing Sarah! Going to use this as a study guide for sure 🔥",
            time = "10:32 AM",
            type = MessageType.TEXT,
        ),
        ChatMessage(
            id = "m5",
            senderId = "me",
            senderName = "Bạn",
            senderAvatar = "https://picsum.photos/seed/me/50/50",
            content = "Same! Also, did anyone join the live session last night? The instructor covered some really advanced stuff.",
            time = "10:35 AM",
            type = MessageType.TEXT,
            isCurrentUser = true,
        ),
    )
}
