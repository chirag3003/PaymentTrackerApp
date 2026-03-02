package codes.chirag.paymenttracker.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.feature.home.HomeUiState
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.ExpenseRed
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1

/**
 * Compact insight summary card shown on the Home screen.
 * Displays 2–3 computed insight messages and a "See full report" button.
 */
@Composable
fun InsightsCard(
    state: HomeUiState,
    onSeeFullReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val insights = computeInsights(state)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceL1)
            .padding(18.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoGraph,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Spending Insights",
                style = MaterialTheme.typography.titleSmall,
                color = OnBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onSeeFullReport) {
                Text(
                    text = "Full report",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangePrimary
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Insight rows
        insights.take(3).forEach { insight ->
            InsightRow(insight)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class InsightItem(
    val message: String,
    val isPositive: Boolean
)

@Composable
private fun InsightRow(insight: InsightItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (insight.isPositive) Icons.Outlined.TrendingDown
                          else Icons.Outlined.TrendingUp,
            contentDescription = null,
            tint = if (insight.isPositive) IncomeGreen else ExpenseRed,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = insight.message,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceMuted
        )
    }
}

fun computeInsights(state: HomeUiState): List<InsightItem> {
    val insights = mutableListOf<InsightItem>()

    // 1. Daily budget pace
    if (state.dailyBudget > 0) {
        val pct = (state.spentToday / state.dailyBudget * 100).toInt()
        when {
            pct == 0    -> insights += InsightItem("Nothing spent today — great start!", isPositive = true)
            pct <= 60   -> insights += InsightItem("Today's spend at $pct% of daily budget — on track.", isPositive = true)
            pct <= 100  -> insights += InsightItem("Used $pct% of today's budget — slow down a bit.", isPositive = false)
            else        -> insights += InsightItem("Over daily budget by ${pct - 100}% today.", isPositive = false)
        }
    }

    // 2. Top category over-budget
    val overBudget = state.categorySpending.filter { it.isOverBudget }
    if (overBudget.isNotEmpty()) {
        val top = overBudget.maxByOrNull { it.amount - it.budget }!!
        insights += InsightItem(
            "${top.category} is over budget by ₹${String.format("%.0f", top.amount - top.budget)}.",
            isPositive = false
        )
    }

    // 3. Biggest spending category
    val topCat = state.categorySpending.maxByOrNull { it.amount }
    if (topCat != null && topCat.amount > 0) {
        insights += InsightItem(
            "Top spend: ${topCat.category} at ₹${String.format("%.0f", topCat.amount)}.",
            isPositive = topCat.amount <= (topCat.budget.takeIf { it > 0 } ?: topCat.amount)
        )
    }

    // 4. Income vs expense health
    if (state.monthlyIncome > 0 && state.monthlyExpense > 0) {
        val savingsRate = ((state.monthlyIncome - state.monthlyExpense) / state.monthlyIncome * 100).toInt()
        when {
            savingsRate >= 20 -> insights += InsightItem("Saving ${savingsRate}% of income — excellent habit!", isPositive = true)
            savingsRate >= 0  -> insights += InsightItem("Saving ${savingsRate}% of income — try to reach 20%.", isPositive = false)
            else              -> insights += InsightItem("Expenses exceed income by ${-savingsRate}%. Review spending.", isPositive = false)
        }
    }

    return insights.ifEmpty {
        listOf(InsightItem("Add transactions to see personalised insights.", isPositive = true))
    }
}
