package com.example.smartreview.ui.screens.payment

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartreview.ui.theme.*

@Composable
fun PaymentMethodScreen(
    navController: NavHostController,
    courseId:      String,
    vm: PaymentMethodViewModel = viewModel(factory = PaymentMethodViewModel.factory(courseId)),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Navigate to PIN screen when payment confirmed
    LaunchedEffect(state.navigateToPin) {
        if (state.navigateToPin) {
            navController.navigate(PaymentRoutes.pinRoute(courseId, state.total))
            vm.onNavigated()
        }
    }

    // Bottom-sheet style: fixed at bottom, dismissable
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.60f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            color    = Color(0xFF131317),
            shape    = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
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
                    modifier         = Modifier
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
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "Thanh toán",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = Primary,
                        )
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, null, tint = OnSurfaceVariant)
                        }
                    }

                    // ── Credit card preview ───────────────────────────────
                    val selected = state.selectedMethod
                    if (selected?.type == PaymentType.CREDIT_CARD) {
                        CreditCardPreview(
                            cardHolder = selected.cardHolder,
                            lastFour   = selected.cardNumber,
                            expiry     = selected.expiry,
                            modifier   = Modifier
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                        )
                    }

                    // ── Payment method list ───────────────────────────────
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier            = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "Phương thức đã lưu",
                            style  = MaterialTheme.typography.labelLarge,
                            color  = OnSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                        state.paymentMethods.forEach { method ->
                            PaymentMethodItem(
                                method     = method,
                                isSelected = method.id == state.selectedMethodId,
                                onSelect   = { vm.selectMethod(method.id) },
                            )
                        }

                        // Add new method button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    BorderStroke(1.dp, Brush.linearGradient(
                                        listOf(OnSurfaceVariant.copy(0.3f), OnSurfaceVariant.copy(0.3f))
                                    )),
                                    RoundedCornerShape(14.dp),
                                )
                                .clickable { /* TODO: add payment method */ },
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Default.AddCircle, null, tint = OnSurfaceVariant)
                                Text(
                                    "Thêm phương thức mới",
                                    color = OnSurfaceVariant,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }

                    // ── Total section ─────────────────────────────────────
                    Surface(
                        color    = Color(0xFF0E0E11),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        shape    = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier            = Modifier.padding(16.dp),
                        ) {
                            TotalRow("Tạm tính",        "%,dđ".format(state.subtotal),     OnSurface)
                            TotalRow("Phí giao dịch",   "Miễn phí",                        OnSurface)
                            HorizontalDivider(color = GlassBorder)
                            TotalRow(
                                label  = "Tổng cộng",
                                value  = "%,dđ".format(state.total),
                                color  = SecondaryDim,
                                bold   = true,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Error ─────────────────────────────────────────────
                    state.errorMessage?.let { err ->
                        Text(
                            err,
                            color    = ErrorColor,
                            style    = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }

                    // ── Pay button ────────────────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (!state.isLoading) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                else Brush.linearGradient(listOf(SurfaceVariant, SurfaceVariant))
                            )
                            .clickable(enabled = !state.isLoading) { vm.confirmPayment() },
                    ) {
                        AnimatedContent(
                            targetState   = state.isLoading,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label         = "payBtn",
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Thanh toán ngay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Terms
                    Text(
                        text      = "Bằng việc thanh toán, bạn đồng ý với Điều khoản dịch vụ",
                        style     = MaterialTheme.typography.labelSmall,
                        color     = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
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
// CREDIT CARD PREVIEW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CreditCardPreview(cardHolder: String, lastFour: String, expiry: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1.58f)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(GradientStart.copy(0.9f), GradientEnd.copy(0.9f))))
            .border(1.dp, Color.White.copy(0.10f), RoundedCornerShape(20.dp)),
    ) {
        // Decorative orbs
        Box(modifier = Modifier.size(200.dp).offset(x = 120.dp, y = (-80).dp)
            .background(Brush.radialGradient(listOf(GradientEnd.copy(0.40f), Color.Transparent)), CircleShape))
        Box(modifier = Modifier.size(150.dp).offset(x = (-50).dp, y = 70.dp)
            .background(Brush.radialGradient(listOf(Secondary.copy(0.20f), Color.Transparent)), CircleShape))

        Column(
            modifier            = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: label + chip icons + contactless
            Row(
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Premium Card", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
                    Row {
                        Box(modifier = Modifier.size(32.dp, 24.dp).background(Tertiary.copy(0.20f), RoundedCornerShape(4.dp))
                            .border(1.dp, Tertiary.copy(0.30f), RoundedCornerShape(4.dp)))
                        Box(modifier = Modifier.size(32.dp, 24.dp).offset(x = (-12).dp)
                            .background(Primary.copy(0.20f), RoundedCornerShape(4.dp))
                            .border(1.dp, Primary.copy(0.30f), RoundedCornerShape(4.dp)))
                    }
                }
                Icon(Icons.Default.Nfc, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(30.dp))
            }

            // Bottom: card number + holder + expiry
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "**** **** **** $lastFour",
                    style         = MaterialTheme.typography.titleMedium,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color         = Color.White,
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Text("Chủ thẻ", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                        Text(cardHolder, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Hết hạn", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                        Text(expiry, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
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
        PaymentType.E_WALLET                            -> Icons.Default.AccountBalanceWallet
    }
    Surface(
        color    = if (isSelected) GlassBg else Color(0xFF0E0E11),
        shape    = RoundedCornerShape(14.dp),
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
            modifier          = Modifier.padding(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint     = if (isSelected) Secondary else OnSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(method.title, style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.SemiBold)
                Text(method.subtitle, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            // Custom radio button
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSelected) Secondary else OnSurfaceVariant, CircleShape),
            ) {
                androidx.compose.animation.AnimatedVisibility(visible = isSelected) {
                    Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Secondary))
                }
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier              = Modifier.fillMaxWidth(),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        Text(
            value,
            style      = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color      = color,
        )
    }
}