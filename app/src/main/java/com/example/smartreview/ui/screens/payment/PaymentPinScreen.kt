package com.example.smartreview.ui.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch

@Composable
fun PaymentPinScreen(
    navController: NavHostController,
    courseId:      String,
    amount:        Long,
    vm: PaymentPinViewModel = viewModel(factory = PaymentPinViewModel.factory(courseId, amount)),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateSuccess) {
        if (state.navigateSuccess) {
            navController.navigate(PaymentRoutes.successRoute(courseId)) {
                popUpTo(PaymentRoutes.methodRoute(courseId)) { inclusive = true }
            }
            vm.onNavigated()
        }
    }

    LaunchedEffect(state.pin) {
        if (state.isComplete) vm.onSubmitPin()
    }

    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(state.triggerShake) {
        if (state.triggerShake) {
            repeat(5) { i ->
                shakeAnim.animateTo(
                    targetValue   = if (i % 2 == 0) 12f else -12f,
                    animationSpec = tween(60, easing = LinearEasing),
                )
            }
            shakeAnim.snapTo(0f)
            vm.onShakeConsumed()
        }
    }

    if (state.showCancelDialog) {
        AlertDialog(
            onDismissRequest  = { vm.onDismissCancelDialog() },
            title             = { Text("Huỷ giao dịch?", color = OnSurface) },
            text              = { Text("Bạn có chắc muốn huỷ thanh toán này không?", color = OnSurfaceVariant) },
            confirmButton     = {
                TextButton(onClick = { navController.popBackStack(); vm.onDismissCancelDialog() }) {
                    Text("Huỷ giao dịch", color = ErrorColor)
                }
            },
            dismissButton     = {
                TextButton(onClick = { vm.onDismissCancelDialog() }) {
                    Text("Tiếp tục", color = Primary)
                }
            },
            containerColor    = Surface,
            titleContentColor = OnSurface,
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
    ) {
        Box(modifier = Modifier.size(350.dp).align(Alignment.TopEnd).offset(x = 80.dp, y = (-80).dp)
            .background(Brush.radialGradient(listOf(GradientStart.copy(0.15f), Color.Transparent)), CircleShape))
        Box(modifier = Modifier.size(280.dp).align(Alignment.BottomStart).offset(x = (-60).dp, y = 80.dp)
            .background(Brush.radialGradient(listOf(Secondary.copy(0.10f), Color.Transparent)), CircleShape))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
                }
                Text(
                    "SMART REVIEW",
                    style = MaterialTheme.typography.titleMedium.copy(
                        brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.weight(0.5f))

            Surface(
                color    = GlassBg,
                shape    = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier            = Modifier.padding(24.dp),
                ) {

                    Surface(
                        color    = SurfaceVariant,
                        shape    = CircleShape,
                        modifier = Modifier.size(64.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.LockOpen, null, tint = Primary, modifier = Modifier.size(32.dp))
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Payment Confirmation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Please enter your 6-digit secure PIN to authorize the transaction of %,dđ.".format(state.amount),
                            style     = MaterialTheme.typography.bodySmall,
                            color     = OnSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .offset(x = shakeAnim.value.dp),
                    ) {
                        repeat(state.pinLength) { index ->
                            val filled = index < state.pin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (filled) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                        else        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                    )
                                    .border(
                                        if (!filled) 2.dp else 0.dp,
                                        if (!filled) OnSurfaceVariant else Color.Transparent,
                                        CircleShape,
                                    ),
                            )
                        }
                    }

                    AnimatedVisibility(visible = state.errorMessage != null) {
                        Text(
                            state.errorMessage ?: "",
                            color    = ErrorColor,
                            style    = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        TransactionChip("Course", state.courseName, modifier = Modifier.weight(1f))
                        TransactionChip("Order ID", state.orderId, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            PinKeypad(
                isEnabled = state.isInputEnabled,
                onDigit   = { vm.onDigit(it) },
                onDelete  = { vm.onDelete() },
                onBio     = { /* biometric placeholder */ },
                modifier  = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (state.isComplete && !state.isProcessing && !state.isLocked)
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        else
                            Brush.linearGradient(listOf(SurfaceVariant, SurfaceVariant))
                    )
                    .clickable(enabled = state.isComplete && !state.isProcessing && !state.isLocked) { vm.onSubmitPin() },
            ) {
                AnimatedContent(state.isProcessing, label = "confirmBtn") { processing ->
                    if (processing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Confirm Payment", color = Color.White, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = {}) {
                    Text("Forgot PIN?", color = Primary, style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = { vm.onShowCancelDialog() }) {
                    Text("Cancel Transaction", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.weight(0.5f))
        }
    }
}

@Composable
private fun PinKeypad(isEnabled: Boolean, onDigit: (String) -> Unit, onDelete: () -> Unit, onBio: () -> Unit, modifier: Modifier = Modifier) {
    val rows = listOf(
        listOf(PinKey.Digit("1"), PinKey.Digit("2", "ABC"), PinKey.Digit("3", "DEF")),
        listOf(PinKey.Digit("4", "GHI"), PinKey.Digit("5", "JKL"), PinKey.Digit("6", "MNO")),
        listOf(PinKey.Digit("7", "PQRS"), PinKey.Digit("8", "TUV"), PinKey.Digit("9", "WXYZ")),
        listOf(PinKey.Biometric, PinKey.Digit("0"), PinKey.Backspace),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = modifier,
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier              = Modifier.fillMaxWidth(),
            ) {
                row.forEach { key ->
                    PinKeyButton(
                        key       = key,
                        enabled   = isEnabled,
                        onDigit   = onDigit,
                        onDelete  = onDelete,
                        onBio     = onBio,
                        modifier  = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private sealed class PinKey {
    data class Digit(val value: String, val sub: String = "") : PinKey()
    object Biometric : PinKey()
    object Backspace : PinKey()
}

@Composable
private fun PinKeyButton(
    key:     PinKey,
    enabled: Boolean,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onBio:   () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSpecial = key is PinKey.Biometric || key is PinKey.Backspace
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (!isSpecial) SurfaceContainer else Color.Transparent)
            .then(if (!isSpecial) Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp)) else Modifier)
            .clickable(enabled = enabled) {
                scope.launch {
                    scale.animateTo(0.90f, tween(60)); scale.animateTo(1f, tween(60))
                }
                when (key) {
                    is PinKey.Digit     -> onDigit(key.value)
                    is PinKey.Backspace -> onDelete()
                    is PinKey.Biometric -> onBio()
                }
            }
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
    ) {
        when (key) {
            is PinKey.Digit     -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(key.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = OnSurface)
                if (key.sub.isNotEmpty())
                    Text(key.sub, fontSize = 8.sp, letterSpacing = 2.sp, color = OnSurfaceVariant)
            }
            is PinKey.Biometric -> Icon(Icons.Default.Fingerprint, null, tint = OnSurfaceVariant, modifier = Modifier.size(26.dp))
            is PinKey.Backspace  -> Icon(Icons.Default.Backspace, null, tint = OnSurfaceVariant, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun TransactionChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = SurfaceContainer, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelLarge, color = OnSurface, maxLines = 1)
        }
    }
}

private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier =
    this.then(Modifier.graphicsLayer(block))