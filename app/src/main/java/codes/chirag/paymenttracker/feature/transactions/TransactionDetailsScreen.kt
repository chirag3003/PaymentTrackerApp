package codes.chirag.paymenttracker.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
 * Transaction Details Screen
 * Displays detailed information about a single transaction
 *
 * @param transactionId The ID of the transaction to display
 * @param onNavigateBack Callback when back button is pressed
 * @param onEdit Callback when edit button is pressed
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: Replace with actual data from ViewModel/Repository
    val transaction = codes.chirag.paymenttracker.feature.transactions.utils.getSampleTransactions()
        .find { it.id == transactionId } ?: return // Handle not found case

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }, actions = {
                TextButton(onClick = onEdit) {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Amount Display
            AmountSection(transaction = transaction)

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction Details
            TransactionDetailsList(transaction = transaction)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Display the transaction amount and title
 */
@Composable
private fun AmountSection(
    transaction: Transaction, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Amount
        Text(
            text = when (transaction.type) {
                TransactionType.INCOME -> "+${formatCurrency(transaction.amount)}"
                TransactionType.EXPENSE -> "-${formatCurrency(transaction.amount)}"
            },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = when (transaction.type) {
                TransactionType.INCOME -> Color(0xFF4CAF50)
                TransactionType.EXPENSE -> Color(0xFFEF5350)
            },
            fontSize = 48.sp
        )

        // Transaction Title
        Text(
            text = transaction.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 18.sp
        )
    }
}

/**
 * List of transaction details (category, date, account, notes)
 */
@Composable
private fun TransactionDetailsList(
    transaction: Transaction, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category
        DetailItem(
            icon = getCategoryIcon(transaction.category),
            iconColor = transaction.color ?: MaterialTheme.colorScheme.primary,
            label = "Category",
            value = transaction.category
        )

        // Date
        DetailItem(
            icon = Icons.Default.CalendarToday,
            iconColor = MaterialTheme.colorScheme.primary,
            label = "Date",
            value = transaction.date
        )

        // Notes (placeholder)
        DetailItem(
            icon = Icons.Default.Description,
            iconColor = MaterialTheme.colorScheme.primary,
            label = "Notes",
            value = "No notes available" // TODO: Add notes field to Transaction model
        )
    }
}

/**
 * Individual detail item row
 */
@Composable
private fun DetailItem(
    icon: ImageVector, iconColor: Color, label: String, value: String, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Label and Value
        Column(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Get icon for transaction category
 */
private fun getCategoryIcon(category: String): ImageVector {
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

