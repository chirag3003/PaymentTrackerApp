package codes.chirag.paymenttracker.core.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a Double as an Indian Rupee string.
 * e.g. 1234.5 → "₹1,234.50"
 */
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    return formatter.format(amount)
}

/**
 * Returns the absolute value formatted as currency, useful when the sign
 * is conveyed by colour (e.g. transaction list items).
 */
fun formatCurrencyAbs(amount: Double): String = formatCurrency(kotlin.math.abs(amount))
