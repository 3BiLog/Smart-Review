package com.example.smartreview.data.model

data class SearchResult(
    val id: String,
    val title: String,
    val instructorName: String,
    val thumbnailUrl: String,
    val price: Long,
    val durationLabel: String,
    val category: String,
    val rating: Float,
    val level: String,
) {
    val formattedPrice: String
        get() = if (price == 0L) "Miễn phí" else "%,dđ".format(price)
}

enum class SortOption(val label: String) {
    POPULAR("Popular"),
    NEWEST("Newest"),
    PRICE_LOW("Giá: Thấp → Cao"),
    PRICE_HIGH("Giá: Cao → Thấp"),
    RATING("Đánh giá cao"),
}
