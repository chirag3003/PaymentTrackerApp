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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codes.chirag.paymenttracker.feature.home.formatCurrency
import codes.chirag.paymenttracker.feature.home.models.Transaction
import codes.chirag.paymenttracker.feature.home.models.TransactionType

/**
 * Individual transaction item component for transaction list
 *
 * @param transaction Transaction data to display
 * @param onClick Callback when transaction item is clicked
 * @param modifier Optional modifier for styling
 */
@Composable
fun TransactionListItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                TransactionIcon(
                    category = transaction.category,
                    color = transaction.color ?: MaterialTheme.colorScheme.primary
                )

                // Title and time
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            // Amount
            Text(
                text = when (transaction.type) {
                    TransactionType.INCOME -> "+${formatCurrency(transaction.amount)}"
                    TransactionType.EXPENSE -> "-${formatCurrency(transaction.amount)}"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    TransactionType.INCOME -> Color(0xFF4CAF50)
                    TransactionType.EXPENSE -> Color(0xFFEF5350)
                },
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Icon component for transaction category
 */
@Composable
private fun TransactionIcon(
    category: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val iconVector = getIconForCategory(category)

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = category,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Get Material Icon based on category
 */
private fun getIconForCategory(category: String): ImageVector {
    return when (category.lowercase()) {
        "food", "dining" -> Icons.Default.Fastfood
        "transport", "travel" -> Icons.Default.Train
        "shopping" -> Icons.Default.ShoppingBag
        "groceries" -> Icons.Default.LocalGroceryStore
        "entertainment" -> Icons.Default.MovieFilter
        "subscription" -> Icons.Default.Subscriptions
        "salary", "income" -> Icons.Default.Work
        else -> Icons.Default.ShoppingBag
    }
}

