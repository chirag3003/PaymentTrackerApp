package codes.chirag.paymenttracker.feature.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.BillingFrequency
import codes.chirag.paymenttracker.core.model.Subscription
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.core.utils.formatCurrency
import codes.chirag.paymenttracker.core.utils.getCategoryMeta
import codes.chirag.paymenttracker.ui.theme.ExpenseRed
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import java.util.Calendar

/**
 * Due-date urgency levels.
 */
private enum class DueStatus { OVERDUE, DUE_SOON, OK }

private fun dueDateStatus(nextDueDateLabel: String): DueStatus {
    return try {
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val parts = nextDueDateLabel.replace(",", "").split(" ")
        val m = months.indexOf(parts[0]) + 1
        val d = parts[1].toInt()
        val y = parts[2].toInt()

        val due = Calendar.getInstance().also { it.set(y, m - 1, d) }
        val today = Calendar.getInstance()
        val diff = (due.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)

        when {
            diff < 0  -> DueStatus.OVERDUE
            diff <= 7 -> DueStatus.DUE_SOON
            else      -> DueStatus.OK
        }
    } catch (_: Exception) {
        DueStatus.OK
    }
}

@Composable
fun SubscriptionListItem(
    subscription: Subscription,
    onToggleActive: (Subscription) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val meta      = getCategoryMeta(subscription.category)
    val status    = dueDateStatus(subscription.nextDueDate)
    val dueLabelColor = when (status) {
        DueStatus.OVERDUE  -> ExpenseRed
        DueStatus.DUE_SOON -> OrangePrimary
        DueStatus.OK       -> OnSurfaceMuted
    }
    val duePrefix = when (status) {
        DueStatus.OVERDUE  -> "Overdue · "
        DueStatus.DUE_SOON -> "Due soon · "
        DueStatus.OK       -> ""
    }

    val amountColor = if (subscription.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    val amountPrefix = if (subscription.type == TransactionType.INCOME) "+" else "-"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category icon bubble
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(meta.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = meta.icon,
                contentDescription = null,
                tint = meta.color,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (subscription.isActive) OnBackground else OnSurfaceMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$duePrefix${subscription.nextDueDate} · ${subscription.frequency.label()}",
                style = MaterialTheme.typography.labelSmall,
                color = dueLabelColor
            )
        }

        // Amount + period badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix${formatCurrency(subscription.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = amountColor,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceL3)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = subscription.frequency.shortLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Toggle active
        IconButton(
            onClick = { onToggleActive(subscription) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (subscription.isActive) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = if (subscription.isActive) "Pause" else "Resume",
                tint = OrangePrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Delete
        IconButton(
            onClick = { onDelete(subscription.id) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete",
                tint = ExpenseRed,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun BillingFrequency.label() = when (this) {
    BillingFrequency.WEEKLY  -> "Weekly"
    BillingFrequency.MONTHLY -> "Monthly"
    BillingFrequency.YEARLY  -> "Yearly"
}

private fun BillingFrequency.shortLabel() = when (this) {
    BillingFrequency.WEEKLY  -> "/wk"
    BillingFrequency.MONTHLY -> "/mo"
    BillingFrequency.YEARLY  -> "/yr"
}
