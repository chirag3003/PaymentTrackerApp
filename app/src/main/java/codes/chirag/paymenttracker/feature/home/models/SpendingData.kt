package codes.chirag.paymenttracker.feature.home.models

import androidx.compose.ui.graphics.Color

/**
 * Data class representing spending for a specific category
 * @param category The name of the spending category (e.g., "Food", "Transport")
 * @param amount The amount spent in this category
 * @param maxAmount The maximum budget for this category
 * @param color Optional color for the category indicator
 */
data class CategorySpending(
    val category: String,
    val amount: Double,
    val maxAmount: Double,
    val color: Color? = null
) {
    /**
     * Calculate the percentage of budget spent
     * @return Percentage value between 0 and 1
     */
    val percentage: Float
        get() = if (maxAmount > 0) (amount / maxAmount).toFloat().coerceIn(0f, 1f) else 0f
}

/**
 * Data class representing overall spending summary
 * @param totalSpent Total amount spent this month
 * @param lastMonthSpent Amount spent last month
 * @param categoryBreakdown List of spending by category
 */
data class SpendingSummary(
    val totalSpent: Double,
    val lastMonthSpent: Double,
    val categoryBreakdown: List<CategorySpending>
) {
    /**
     * Calculate percentage change from last month
     * @return Percentage change (positive means increase)
     */
    val percentageChange: Float
        get() = if (lastMonthSpent > 0) {
            ((totalSpent - lastMonthSpent) / lastMonthSpent * 100).toFloat()
        } else {
            0f
        }

    /**
     * Check if spending increased compared to last month
     */
    val isIncrease: Boolean
        get() = percentageChange > 0
}
