package codes.chirag.paymenttracker.core.utils

import codes.chirag.paymenttracker.core.model.BillingFrequency
import java.util.Calendar

object DateUtils {

    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    /**
     * Formats a Calendar instance to "MMM d, yyyy" string
     */
    fun formatCalendarLabel(cal: Calendar): String {
        return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    /**
     * Parses "MMM d, yyyy" string back into a Calendar instance.
     * Returns null if parsing fails.
     */
    fun parseDateLabel(label: String): Calendar? {
        return try {
            val parts = label.replace(",", "").split(" ")
            if (parts.size != 3) return null
            val m = months.indexOf(parts[0])
            val d = parts[1].toInt()
            val y = parts[2].toInt()

            if (m == -1) return null

            Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                // Clear time fields for accurate day comparisons
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if the given "MMM d, yyyy" label is today or in the past.
     */
    fun isDueOrPast(dateLabel: String): Boolean {
        val targetCal = parseDateLabel(dateLabel) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return targetCal.timeInMillis <= today.timeInMillis
    }

    /**
     * Returns the next due date by adding the BillingFrequency to the current due date.
     */
    fun calculateNextDueDate(currentDateLabel: String, frequency: BillingFrequency): String {
        val cal = parseDateLabel(currentDateLabel) ?: Calendar.getInstance()
        when (frequency) {
            BillingFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            BillingFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            BillingFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return formatCalendarLabel(cal)
    }
}