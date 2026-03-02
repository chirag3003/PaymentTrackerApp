package codes.chirag.paymenttracker.core.model

/**
 * Whether a transaction adds money or removes it.
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Payment method used for the transaction.
 * Captures the student-centric options outlined in the product spec.
 */
enum class PaymentMethod {
    UPI,
    CARD,
    CASH,
    WALLET,
    OTHER
}

/**
 * A single financial transaction.
 *
 * @param id           Unique identifier (UUID string; DB-generated in the future).
 * @param title        Short description shown in lists (e.g. "Starbucks", "Salary").
 * @param amount       Always positive; sign is determined by [type].
 * @param type         [TransactionType.INCOME] or [TransactionType.EXPENSE].
 * @param category     Category label (e.g. "Food", "Transport"). Maps to an icon via
 *                     [codes.chirag.paymenttracker.core.utils.getCategoryMeta].
 * @param date         Display-ready date string (e.g. "Today", "Feb 28, 2026").
 * @param paymentMethod How the transaction was made.
 * @param notes        Optional free-text notes added by the user.
 * @param tags         Optional tags for cross-category grouping (e.g. "Trip", "Club").
 */
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: String,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val notes: String = "",
    val tags: List<String> = emptyList()
)

/**
 * Spending data for a single budget category.
 *
 * @param category   Category name.
 * @param amount     Amount spent so far in the current period.
 * @param budget     The budget cap for this category (0 = no cap set).
 */
data class CategorySpending(
    val category: String,
    val amount: Double,
    val budget: Double
) {
    /** Fraction of budget consumed, clamped to [0, 1]. Returns 0 if no budget is set. */
    val progress: Float
        get() = if (budget > 0) (amount / budget).toFloat().coerceIn(0f, 1f) else 0f

    /** True when spending has exceeded the budget. */
    val isOverBudget: Boolean
        get() = budget > 0 && amount > budget
}

/**
 * Aggregated monthly spending summary shown on the Home dashboard.
 *
 * @param totalBudget       The user's total monthly budget / allowance.
 * @param totalSpent        Amount spent so far this month.
 * @param lastMonthSpent    Amount spent last month (for trend comparison).
 * @param categoryBreakdown Per-category breakdown for the current month.
 */
data class SpendingSummary(
    val totalBudget: Double,
    val totalSpent: Double,
    val lastMonthSpent: Double,
    val categoryBreakdown: List<CategorySpending>
) {
    /** Remaining budget this month. Can be negative if over budget. */
    val remaining: Double get() = totalBudget - totalSpent

    /** Month-over-month percentage change (positive = spent more than last month). */
    val monthOverMonthChange: Float
        get() = if (lastMonthSpent > 0) {
            ((totalSpent - lastMonthSpent) / lastMonthSpent * 100).toFloat()
        } else 0f

    /** True when this month's spend is higher than last month's. */
    val isSpendingUp: Boolean get() = totalSpent > lastMonthSpent
}

/**
 * A savings goal set by the user.
 *
 * @param id         Unique identifier.
 * @param name       Goal name (e.g. "New Phone", "Goa Trip").
 * @param targetAmount  Amount to be saved.
 * @param savedAmount   Amount saved so far.
 * @param targetDate    Human-readable target date string (e.g. "Mar 2026").
 */
data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val targetDate: String = ""
) {
    /** Fraction of target saved, clamped to [0, 1]. */
    val progress: Float
        get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    /** True when the goal has been fully funded. */
    val isCompleted: Boolean get() = savedAmount >= targetAmount
}

/**
 * Recurring billing frequency.
 */
enum class BillingFrequency { WEEKLY, MONTHLY, YEARLY }

/**
 * A recurring subscription or bill tracked by the user.
 *
 * @param id            Unique identifier.
 * @param name          Subscription name (e.g. "Netflix", "Gym").
 * @param amount        Billing amount per cycle.
 * @param frequency     How often billing occurs.
 * @param nextDueDate   Display string for the next due date (e.g. "Mar 10, 2026").
 * @param category      Category label.
 * @param paymentMethod How the subscription is charged.
 * @param isActive      Whether the subscription is currently active.
 */
data class Subscription(
    val id: String,
    val name: String,
    val amount: Double,
    val frequency: BillingFrequency,
    val nextDueDate: String,
    val category: String,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val isActive: Boolean = true
)
