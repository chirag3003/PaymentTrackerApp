package codes.chirag.paymenttracker.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 5

@Composable
fun OnboardingScreen(
    onComplete: (name: String, monthlyBudget: Double, preferredMethod: PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    // Collected user data across pages
    var name           by rememberSaveable { mutableStateOf("") }
    var budgetInput    by rememberSaveable { mutableStateOf("") }
    var paymentMethod  by rememberSaveable { mutableStateOf(PaymentMethod.UPI) }

    val isLastPage = pagerState.currentPage == PAGE_COUNT - 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> NamePage(name = name, onNameChange = { name = it })
                2 -> BudgetPage(budget = budgetInput, onBudgetChange = { budgetInput = it })
                3 -> PaymentMethodPage(selected = paymentMethod, onSelect = { paymentMethod = it })
                4 -> SummaryPage(name = name, budget = budgetInput, method = paymentMethod)
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Dot indicator
            DotIndicator(
                pageCount = PAGE_COUNT,
                currentPage = pagerState.currentPage
            )

            // Primary action button
            Button(
                onClick = {
                    if (isLastPage) {
                        val budget = budgetInput.toDoubleOrNull() ?: 0.0
                        onComplete(name.trim(), budget, paymentMethod)
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = OnPrimary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLastPage) "Start Tracking" else "Continue",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Icon(
                        imageVector = if (isLastPage) Icons.Outlined.Check else Icons.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Skip link (not shown on last page)
            if (!isLastPage) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceMuted,
                    modifier = Modifier.clickable {
                        scope.launch {
                            pagerState.animateScrollToPage(PAGE_COUNT - 1)
                        }
                    }
                )
            }
        }
    }
}

// ── Dot indicator ─────────────────────────────────────────────────────────────

@Composable
private fun DotIndicator(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 6.dp,
                animationSpec = tween(300),
                label = "dot_width"
            )
            val color by animateColorAsState(
                targetValue = if (isActive) OrangePrimary else SurfaceL3,
                animationSpec = tween(300),
                label = "dot_color"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// ── Page 1 — Welcome ──────────────────────────────────────────────────────────

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero illustration — Canvas-drawn coins/chart motif
        WelcomeIllustration()
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "PaymentTracker",
            style = MaterialTheme.typography.displaySmall,
            color = OrangePrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Smart money management\nfor college life",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WelcomeIllustration() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val orange = OrangePrimary
        val orangeSubtle = OrangeSubtle
        val surfaceL1 = SurfaceL1

        // Outer ring
        drawCircle(
            color = orangeSubtle,
            radius = cx * 0.9f,
            center = Offset(cx, cy)
        )

        // Three stacked coin circles
        val coinRadius = cx * 0.22f
        val coinOffsets = listOf(
            Offset(cx - coinRadius * 1.1f, cy + coinRadius * 0.3f),
            Offset(cx, cy - coinRadius * 0.4f),
            Offset(cx + coinRadius * 1.1f, cy + coinRadius * 0.3f)
        )
        coinOffsets.forEach { offset ->
            drawCircle(color = surfaceL1, radius = coinRadius + 4.dp.toPx(), center = offset)
            drawCircle(color = orange, radius = coinRadius, center = offset)
            drawCircle(
                color = orange.copy(alpha = 0.4f),
                radius = coinRadius * 0.6f,
                center = offset,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Bar chart bars at the bottom
        val barW = cx * 0.12f
        val barMaxH = cy * 0.45f
        val barHeights = listOf(0.5f, 0.8f, 0.4f, 1.0f, 0.65f)
        val totalBarsWidth = barHeights.size * barW + (barHeights.size - 1) * barW * 0.5f
        var barX = cx - totalBarsWidth / 2f
        val barBottom = cy + cy * 0.78f
        barHeights.forEach { frac ->
            val h = barMaxH * frac
            drawRoundRect(
                color = orange.copy(alpha = if (frac == 1.0f) 1f else 0.4f),
                topLeft = Offset(barX, barBottom - h),
                size = Size(barW, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 3f)
            )
            barX += barW * 1.5f
        }

        // Upward arrow arc (trend line)
        drawArc(
            color = IncomeGreen.copy(alpha = 0.7f),
            startAngle = 180f,
            sweepAngle = -90f,
            useCenter = false,
            topLeft = Offset(cx - cx * 0.55f, cy - cy * 0.35f),
            size = Size(cx * 1.1f, cy * 0.7f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// ── Page 2 — Name ─────────────────────────────────────────────────────────────

@Composable
private fun NamePage(name: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PageIcon(Icons.Outlined.AccountCircle, OrangePrimary)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.headlineMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Your name will appear on the home screen",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Your first name", color = OnSurfaceMuted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            colors = onboardingTextFieldColors(),
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnBackground)
        )
    }
}

// ── Page 3 — Monthly Budget ───────────────────────────────────────────────────

@Composable
private fun BudgetPage(budget: String, onBudgetChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PageIcon(Icons.Outlined.CurrencyRupee, OrangePrimary)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Set your monthly budget",
            style = MaterialTheme.typography.headlineMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "How much do you get per month?\nAllowance, stipend, or salary — all counts.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))
        OutlinedTextField(
            value = budget,
            onValueChange = onBudgetChange,
            placeholder = { Text("e.g. 15000", color = OnSurfaceMuted) },
            leadingIcon = {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceMuted,
                    modifier = Modifier.padding(start = 4.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = onboardingTextFieldColors(),
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnBackground)
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Preset quick-picks
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("5000", "10000", "15000", "20000").forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (budget == preset) OrangePrimary else SurfaceL3)
                        .border(
                            0.5.dp,
                            if (budget == preset) OrangePrimary else DividerColor,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onBudgetChange(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "₹$preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (budget == preset) OnPrimary else OnSurfaceMuted
                    )
                }
            }
        }
    }
}

// ── Page 4 — Payment Method ───────────────────────────────────────────────────

@Composable
private fun PaymentMethodPage(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PageIcon(Icons.Outlined.Payments, OrangePrimary)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "How do you mostly pay?",
            style = MaterialTheme.typography.headlineMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "We'll use this as the default when you add transactions",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        // Method tiles in a 2-column grid
        val methods = PaymentMethod.entries
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            methods.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { method ->
                        val isSelected = method == selected
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) OrangePrimary else SurfaceL1)
                                .border(
                                    1.dp,
                                    if (isSelected) OrangePrimary else BorderColor,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelect(method) }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = methodEmoji(method),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = method.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) OnPrimary else OnBackground
                                )
                            }
                        }
                    }
                    // Fill empty cell if odd number
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun methodEmoji(method: PaymentMethod): String = when (method) {
    PaymentMethod.UPI    -> "📱"
    PaymentMethod.CARD   -> "💳"
    PaymentMethod.CASH   -> "💵"
    PaymentMethod.WALLET -> "👛"
    PaymentMethod.OTHER  -> "🔄"
}

// ── Page 5 — Summary / All set ────────────────────────────────────────────────

@Composable
private fun SummaryPage(name: String, budget: String, method: PaymentMethod) {
    val displayName = name.ifBlank { "there" }
    val displayBudget = budget.toDoubleOrNull()?.let { "₹${it.toLong()}" } ?: "Not set"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(OrangePrimary, OrangeSubtle)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = OnPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "You're all set, $displayName!",
            style = MaterialTheme.typography.headlineMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Here's a quick summary of your setup",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))

        // Summary card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceL1)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryRow(label = "Name", value = displayName)
                SummaryDivider()
                SummaryRow(label = "Monthly Budget", value = displayBudget)
                SummaryDivider()
                SummaryRow(label = "Default Payment", value = method.name)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(DividerColor)
    )
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun PageIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(OrangeSubtle),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun onboardingTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OnBackground,
    unfocusedTextColor = OnBackground,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = BorderColor,
    cursorColor = OrangePrimary,
    focusedContainerColor = SurfaceL3,
    unfocusedContainerColor = SurfaceL3
)
