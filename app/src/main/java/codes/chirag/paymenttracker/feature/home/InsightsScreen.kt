package codes.chirag.paymenttracker.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.CategorySpending
import codes.chirag.paymenttracker.core.utils.getCategoryMeta
import codes.chirag.paymenttracker.core.utils.formatCurrency
import codes.chirag.paymenttracker.feature.home.components.InsightItem
import codes.chirag.paymenttracker.feature.home.components.WeeklyBarData
import codes.chirag.paymenttracker.feature.home.components.computeInsights
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.ExpenseRed
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnSurface
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL2
import codes.chirag.paymenttracker.ui.theme.SurfaceL3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val insights = computeInsights(state)

    Scaffold(
        modifier = modifier,
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Spending Report",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
                start = 0.dp,
                end = 0.dp
            )
        ) {
            // ── Summary row ───────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip(
                        label = "Income",
                        value = formatCurrency(state.monthlyIncome),
                        color = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryChip(
                        label = "Expenses",
                        value = formatCurrency(state.monthlyExpense),
                        color = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryChip(
                        label = "Balance",
                        value = formatCurrency(state.balance),
                        color = OrangePrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Pie chart ─────────────────────────────────────────────────────
            if (state.categorySpending.isNotEmpty()) {
                item {
                    SectionTitle("Category Breakdown", modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    CategoryPieChart(
                        categories = state.categorySpending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Category legend rows
                items(state.categorySpending) { cat ->
                    CategoryInsightRow(
                        cat = cat,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // ── Weekly bar chart ──────────────────────────────────────────────
            if (state.insightsWeeklySpending.isNotEmpty()) {
                item {
                    SectionTitle("Weekly Trend", modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightsBarChart(
                        data       = state.insightsWeeklySpending,
                        weekLabel  = state.insightsWeekLabel,
                        onPrevWeek = { viewModel.insightsWeekPrev() },
                        onNextWeek = { viewModel.insightsWeekNext() },
                        canGoNext  = state.insightsWeekOffset < 0,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ── Insights list ─────────────────────────────────────────────────
            item {
                SectionTitle("Key Insights", modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(insights) { insight ->
                InsightDetailRow(
                    insight = insight,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = OnBackground,
        modifier = modifier
    )
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceL1)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceMuted
        )
    }
}

/**
 * Canvas-drawn donut (pie) chart for category breakdown.
 */
@Composable
private fun CategoryPieChart(
    categories: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    val total = categories.sumOf { it.amount }.toFloat()
    if (total == 0f) return

    val colors = categories.map { getCategoryMeta(it.category).color }

    Box(
        modifier = modifier.height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 36.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            var startAngle = -90f
            categories.forEachIndexed { i, cat ->
                val sweep = (cat.amount.toFloat() / total) * 360f
                drawArc(
                    color = colors.getOrElse(i) { OrangePrimary },
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f, // gap between segments
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }
        // Centre label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatCurrency(total.toDouble()),
                style = MaterialTheme.typography.titleMedium,
                color = OnBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "total",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted
            )
        }
    }
}

@Composable
private fun CategoryInsightRow(
    cat: CategorySpending,
    modifier: Modifier = Modifier
) {
    val meta = getCategoryMeta(cat.category)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceL1)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(meta.color)
        )
        Text(
            text = cat.category,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurface,
            modifier = Modifier.weight(1f)
        )
        if (cat.budget > 0) {
            Text(
                text = "${(cat.progress * 100).toInt()}% of budget",
                style = MaterialTheme.typography.labelSmall,
                color = if (cat.isOverBudget) ExpenseRed else OnSurfaceMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = formatCurrency(cat.amount),
            style = MaterialTheme.typography.labelMedium,
            color = OnBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Simplified bar chart for the Insights screen weekly trend with week navigation.
 */
@Composable
private fun InsightsBarChart(
    data: List<WeeklyBarData>,
    weekLabel: String = "",
    onPrevWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {},
    canGoNext: Boolean = false,
    modifier: Modifier = Modifier
) {
    val maxAmount = data.maxOfOrNull { it.amount }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceL1)
            .padding(16.dp)
    ) {
        // ── Week navigation header ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevWeek,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Previous week",
                    tint = OnSurfaceMuted
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Spending",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
                if (weekLabel.isNotBlank()) {
                    Text(
                        text = weekLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = OnBackground
                    )
                }
            }
            IconButton(
                onClick = onNextWeek,
                enabled = canGoNext,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Next week",
                    tint = if (canGoNext) OnSurfaceMuted else OnSurfaceMuted.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Bars ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { bar ->
                val fraction = (bar.amount.toFloat() / maxAmount).coerceIn(0f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height((100 * fraction).dp.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(OrangePrimary.copy(alpha = 0.7f + 0.3f * fraction))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bar.day,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightDetailRow(
    insight: InsightItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceL1)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (insight.isPositive) Icons.Outlined.TrendingDown
                          else Icons.Outlined.TrendingUp,
            contentDescription = null,
            tint = if (insight.isPositive) IncomeGreen else ExpenseRed,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = insight.message,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurface
        )
    }
}
