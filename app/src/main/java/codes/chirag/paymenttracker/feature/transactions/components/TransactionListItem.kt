package codes.chirag.paymenttracker.feature.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.core.utils.formatCurrency
import codes.chirag.paymenttracker.core.utils.getCategoryMeta
import codes.chirag.paymenttracker.ui.theme.ExpenseRed
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted

@Composable
fun TransactionListItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val meta = getCategoryMeta(transaction.category)
    val amountColor  = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "-"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackground,
                maxLines = 1
            )
            Text(
                text = transaction.category + if (transaction.paymentMethod.name.isNotEmpty())
                    " · ${transaction.paymentMethod.name}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                maxLines = 1
            )
        }
        Text(
            text = "$amountPrefix${formatCurrency(transaction.amount)}",
            style = MaterialTheme.typography.titleSmall,
            color = amountColor
        )
    }
}
