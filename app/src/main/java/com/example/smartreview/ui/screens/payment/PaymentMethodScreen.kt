package com.example.smartreview.ui.screens.payment

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    navController: NavHostController,
    courseId: String,
    courseName: String = "",
    coursePrice: Long = 0,
    vm: PaymentMethodViewModel = viewModel(
        factory = PaymentMethodViewModel.factory(courseId, courseName, coursePrice)
    ),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(state.checkoutUrl) {
        state.checkoutUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            vm.consumeCheckoutUrl()
        }
    }

    LaunchedEffect(state.paymentSuccess) {
        if (state.paymentSuccess) {
            vm.consumePaymentSuccess()
            navController.navigate(PaymentRoutes.successRoute(courseId, justPaid = true)) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.onReturnedFromCheckout()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // Bottom-sheet style: fixed at bottom, dismissable
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.60f))
            .clickable { navController.popBackStack() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            color = Color(0xFF131317),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                // Drag handle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(OnSurfaceVariant.copy(0.3f))
                    )
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // ── Header ────────────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "Thanh toán",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, null, tint = OnSurfaceVariant)
                        }
                    }

                    // ── Course Info ────────────────────────────────────────
                    Surface(
                        color = SurfaceContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp),
                        ) {
                            Icon(
                                Icons.Default.School,
                                null,
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    state.courseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurface,
                                    maxLines = 1
                                )
                                Text(
                                    "Giá: ${String.format("%,d", state.coursePrice)}đ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary
                                )
                            }
                        }
                    }

                    // ── Payment method list ───────────────────────────────
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "Phương thức thanh toán",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                        )

                        state.paymentMethods.forEach { method ->
                            PaymentMethodItem(
                                method = method,
                                isSelected = method.id == state.selectedMethodId,
                                onSelect = { vm.selectMethod(method.id) },
                            )
                        }
                    }

                    // ── Total section ─────────────────────────────────────
                    Surface(
                        color = Color(0xFF0E0E11),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(16.dp),
                        ) {
                            TotalRow("Tạm tính", String.format("%,dđ", state.subtotal), OnSurface)
                            TotalRow("Phí giao dịch", "Miễn phí", OnSurface)
                            HorizontalDivider(color = GlassBorder)
                            TotalRow(
                                label = "Tổng cộng",
                                value = String.format("%,dđ", state.total),
                                color = SecondaryDim,
                                bold = true,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Error ─────────────────────────────────────────────
                    state.errorMessage?.let { err ->
                        Text(
                            err,
                            color = ErrorColor,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }

                    // ── Pay button ────────────────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (!state.isProcessingPayment)
                                    Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                else
                                    Brush.linearGradient(listOf(SurfaceVariant, SurfaceVariant))
                            )
                            .clickable(enabled = !state.isProcessingPayment) {
                                vm.confirmPayment()
                            },
                    ) {
                        AnimatedContent(
                            targetState = state.isProcessingPayment,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "payBtn",
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Thanh toán ngay",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Terms
                    Text(
                        text = "Bằng việc thanh toán, bạn đồng ý với Điều khoản dịch vụ",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 20.dp),
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAYMENT METHOD ITEM
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PaymentMethodItem(method: PaymentMethodOption, isSelected: Boolean, onSelect: () -> Unit) {
    val icon: ImageVector = when (method.type) {
        PaymentType.CREDIT_CARD, PaymentType.DEBIT_CARD -> Icons.Default.CreditCard
        PaymentType.E_WALLET -> Icons.Default.AccountBalanceWallet
    }

    Surface(
        color = if (isSelected) GlassBg else Color(0xFF0E0E11),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isSelected) Secondary.copy(0.4f) else GlassBorder,
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onSelect),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) Secondary else OnSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    method.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    method.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
            // Custom radio button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSelected) Secondary else OnSurfaceVariant, CircleShape),
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Secondary),
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color,
        )
    }
}