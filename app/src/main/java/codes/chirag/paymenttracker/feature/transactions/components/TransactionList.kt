package codes.chirag.paymenttracker.feature.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codes.chirag.paymenttracker.feature.home.models.Transaction

/**
 * Grouped transaction list component
 * Groups transactions by date sections
 *
 * @param transactions List of transactions to display
 * @param onTransactionClick Callback when a transaction is clicked
 * @param modifier Optional modifier for styling
 */
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty()) {
        EmptyTransactionsView()
    } else {
        // Group transactions by date
        val groupedTransactions = transactions.groupBy { it.date }

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedTransactions.forEach { (date, transactionsForDate) ->
                TransactionDateGroup(
                    dateLabel = date,
                    transactions = transactionsForDate,
                    onTransactionClick = onTransactionClick
                )
            }
        }
    }
}

/**
 * Transaction group for a specific date
 */
@Composable
private fun TransactionDateGroup(
    dateLabel: String,
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date header
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Transactions for this date
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transactions.forEach { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    onClick = onTransactionClick
                )
            }
        }
    }
}

/**
 * Empty state view when there are no transactions
 */
@Composable
private fun EmptyTransactionsView(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No transactions found",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Your transactions will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}


