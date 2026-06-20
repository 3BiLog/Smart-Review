package com.example.smartreview.ui.screens.coursereviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.screens.coursedetail.RatingSummaryCard
import com.example.smartreview.ui.screens.coursedetail.ReviewItem
import com.example.smartreview.ui.theme.*

const val COURSE_REVIEWS_ROUTE = "course_reviews/{courseId}?courseTitle={courseTitle}"

fun courseReviewsRoute(courseId: String, courseTitle: String): String {
    val encodedTitle = java.net.URLEncoder.encode(courseTitle, Charsets.UTF_8.name())
    return "course_reviews/$courseId?courseTitle=$encodedTitle"
}

@Composable
fun CourseReviewsScreen(
    navController: NavHostController,
    courseId: String,
    courseTitle: String,
    vm: CourseReviewsViewModel = viewModel(
        factory = CourseReviewsViewModel.provideFactory(courseId, courseTitle),
    ),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    val displayTitle = if (state.courseTitle.isBlank()) courseTitle else state.courseTitle

    Scaffold(
        containerColor = Background,
        topBar = {
            CourseReviewsTopBar(
                courseTitle = displayTitle,
                onBack = { navController.popBackStack() },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                }
            }

            state.reviews.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, null, tint = OnSurfaceVariant, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Chưa có đánh giá nào",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface,
                        )
                        Text(
                            "Hãy quay lại trang khóa học để viết đánh giá đầu tiên.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp),
                        )
                    }
                }
            }

            else -> {
                val summary = state.summary
                val userReview = state.userReview
                val otherReviews = state.reviews.filter { review ->
                    review.id != userReview?.id
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (summary != null) {
                        item(key = "summary") {
                            RatingSummaryCard(summary = summary)
                        }
                    }

                    if (userReview != null) {
                        item(key = "user-review-header") {
                            Text(
                                "Đánh giá của bạn",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                            )
                        }
                        item(key = "user-review") {
                            ReviewItem(
                                review = userReview,
                                isUserReview = true,
                                onHelpfulClick = { vm.onHelpfulClick(userReview.id) },
                                onEditClick = { navController.popBackStack() },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    if (otherReviews.isNotEmpty()) {
                        item(key = "other-reviews-header") {
                            Text(
                                if (userReview != null) "Đánh giá từ học viên khác" else "Tất cả đánh giá",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        otherReviews.forEach { review ->
                            item(key = review.id) {
                                ReviewItem(
                                    review = review,
                                    isUserReview = false,
                                    onHelpfulClick = { vm.onHelpfulClick(review.id) },
                                    onEditClick = {},
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseReviewsTopBar(courseTitle: String, onBack: () -> Unit) {
    Surface(color = GlassBg, tonalElevation = 0.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Đánh giá khóa học",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                )
                Text(
                    courseTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
