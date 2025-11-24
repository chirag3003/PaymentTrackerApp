package codes.chirag.paymenttracker.feature.home

import java.text.NumberFormat
import java.util.Locale

/**
 * Format a double value as currency
 */
internal fun formatCurrency(amount: Double): String {
    return "₹ $amount"
}
