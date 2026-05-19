package com.example.smartreview.data.mock

import com.example.smartreview.data.model.LeaderboardEntry

object MockLeaderboardData {

    /** Base scores at [LeaderboardTab.THIS_WEEK] scale; ViewModel applies tab multipliers. */
    val baseEntries = listOf(
        LeaderboardEntry(1, "u1", "Hoàng T.", "https://picsum.photos/seed/lb1/80/80", 12_200, 0.82f),
        LeaderboardEntry(2, "u2", "Linh N.", "https://picsum.photos/seed/lb2/80/80", 8_450, 0.65f),
        LeaderboardEntry(3, "u3", "Mai V.", "https://picsum.photos/seed/lb3/80/80", 7_900, 0.60f),
        LeaderboardEntry(4, "u4", "Thành Đ.", "https://picsum.photos/seed/lb4/50/50", 6_500, 0.52f),
        LeaderboardEntry(5, "me", "Bạn", "https://picsum.photos/seed/lbme/50/50", 5_800, 0.46f, true),
        LeaderboardEntry(6, "u6", "Quân P.", "https://picsum.photos/seed/lb6/50/50", 4_200, 0.34f),
        LeaderboardEntry(7, "u7", "Bích N.", "https://picsum.photos/seed/lb7/50/50", 3_100, 0.25f),
    )
}
