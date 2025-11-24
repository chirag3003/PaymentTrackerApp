package codes.chirag.paymenttracker.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codes.chirag.paymenttracker.feature.home.formatCurrency
import codes.chirag.paymenttracker.feature.home.models.SpendingSummary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * A section displaying spending summary for the current month
 *
 * @param spendingSummary The spending data to display
 * @param modifier Optional modifier for styling
 */
@Composable
fun SpendingSection(
    spendingSummary: SpendingSummary,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Text(
            text = "Spending This Month",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total amount and comparison
                SpendingHeader(
                    totalSpent = spendingSummary.totalSpent,
                    percentageChange = spendingSummary.percentageChange,
                    isIncrease = spendingSummary.isIncrease
                )

                // Category breakdown
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    spendingSummary.categoryBreakdown.forEach { category ->
                        CategorySpendingItem(
                            categorySpending = category
                        )
                    }
                }
            }
        }
    }
}

/**
 * Header showing total spending amount and percentage change
 */
@Composable
private fun SpendingHeader(
    totalSpent: Double,
    percentageChange: Float,
    isIncrease: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Total amount
        Text(
            text = formatCurrency(totalSpent),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Percentage change indicator
        if (percentageChange != 0f) {
            PercentageChangeChip(
                percentageChange = percentageChange,
                isIncrease = isIncrease
            )
        }
    }
}

/**
 * Chip displaying percentage change compared to last month
 */
@Composable
private fun PercentageChangeChip(
    percentageChange: Float,
    isIncrease: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isIncrease) {
        Color(0xFFFFEBEE) // Light red for increase
    } else {
        Color(0xFFE8F5E9) // Light green for decrease
    }

    val textColor = if (isIncrease) {
        Color(0xFFC62828) // Red for increase
    } else {
        Color(0xFF2E7D32) // Green for decrease
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isIncrease) "+" else "",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontSize = 12.sp
        )
        Text(
            text = "${percentageChange.absoluteValue.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontSize = 12.sp
        )
        Text(
            text = "vs last month",
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
    }
}

