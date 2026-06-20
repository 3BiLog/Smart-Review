package com.example.smartreview.ui.screens.coursedetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartreview.data.model.Review
import com.example.smartreview.data.model.ReviewSummary
import com.example.smartreview.ui.theme.*

@Composable
fun CourseDetailReviewSection(
    summary: ReviewSummary?,
    userReview: Review?,
    totalReviewCount: Int,
    isLoading: Boolean,
    onWriteReview: () -> Unit,
    onEditReview: () -> Unit,
    onViewAllReviews: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "Đánh giá",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
            )
            if (totalReviewCount > 0) {
                Text(
                    "$totalReviewCount đánh giá",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Primary,
                    strokeWidth = 2.dp,
                )
            }
            return@Column
        }

        val hasReviews = totalReviewCount > 0
        val displaySummary = summary ?: userReview?.let {
            ReviewSummary(
                averageRating = it.rating.toFloat(),
                totalReviews = 1,
                ratingDistribution = mapOf(it.rating to 1),
            )
        }

        if (displaySummary != null && displaySummary.totalReviews > 0) {
            RatingSummaryCard(summary = displaySummary)
            Spacer(modifier = Modifier.height(16.dp))
        } else if (!hasReviews) {
            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.Star, null, tint = OnSurfaceVariant, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Chưa có đánh giá nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                    )
                    Text(
                        "Hãy là người đầu tiên đánh giá khóa học này!",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (userReview != null) {
            Text(
                "Đánh giá của bạn",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            ReviewItem(
                review = userReview,
                isUserReview = true,
                onHelpfulClick = {},
                onEditClick = onEditReview,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (totalReviewCount > 0) {
            TextButton(
                onClick = onViewAllReviews,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Xem tất cả $totalReviewCount đánh giá",
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (userReview == null) {
            Button(
                onClick = onWriteReview,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Star, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Viết đánh giá", color = Color.White)
            }
        }
    }
}

@Composable
fun RatingSummaryCard(summary: ReviewSummary) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    String.format("%.1f", summary.averageRating),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                )
                Text(
                    "trên 5",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                )
                RatingStars(rating = summary.averageRating.toInt(), modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                (5 downTo 1).forEach { star ->
                    val count = summary.ratingDistribution[star] ?: 0
                    val percentage = if (summary.totalReviews > 0) count.toFloat() / summary.totalReviews else 0f
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$star",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.width(16.dp),
                        )
                        Icon(Icons.Default.Star, null, tint = OnSurfaceVariant, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Primary,
                            trackColor = SurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.width(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingStars(rating: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            Icon(
                if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                null,
                tint = if (index < rating) Secondary else OnSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun ReviewItem(
    review: Review,
    isUserReview: Boolean,
    onHelpfulClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = review.userAvatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        review.userName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface,
                    )
                    if (isUserReview) {
                        Text(
                            "Bạn đã đánh giá",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                RatingStars(rating = review.rating)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                review.content,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Text(
                    review.createdAt.toDate().toLocaleString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onHelpfulClick,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    review.helpfulCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                )
                if (isUserReview) {
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(
                        onClick = onEditClick,
                        modifier = Modifier.height(24.dp),
                    ) {
                        Text("Sửa", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
