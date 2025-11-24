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
        get() = totalSpent > lastMonthSpent
}

/**
 * Enum representing the type of transaction
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Data class representing a single transaction
 * @param id Unique identifier for the transaction
 * @param title Name or description of the transaction
 * @param amount Amount of money involved
 * @param type Type of transaction (income or expense)
 * @param category Category of the transaction
 * @param date Date of the transaction
 * @param icon Optional icon name for the transaction category
 */
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: String, // Format: "MMM dd, yyyy"
    val icon: String? = null,
    val color: Color? = null
)

