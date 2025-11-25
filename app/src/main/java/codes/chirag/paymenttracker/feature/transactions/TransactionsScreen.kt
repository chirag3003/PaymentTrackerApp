package codes.chirag.paymenttracker.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.feature.home.models.Transaction
import codes.chirag.paymenttracker.feature.transactions.components.SearchBar
import codes.chirag.paymenttracker.feature.transactions.components.TransactionList
import codes.chirag.paymenttracker.feature.transactions.utils.getSampleTransactions

/**
 * Transactions screen displaying all user transactions with search functionality
 *
 * @param modifier Optional modifier for styling
 */
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier
) {
    // Setting up scroll
    val scrollState = rememberScrollState()

    // Search query state
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Get sample transactions (TODO: Replace with actual data from ViewModel/Repository)
    val allTransactions = remember { getSampleTransactions() }

    // Filter transactions based on search query
    val filteredTransactions by remember(searchQuery, allTransactions) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                allTransactions
            } else {
                allTransactions.filter { transaction ->
                    transaction.title.contains(searchQuery, ignoreCase = true) ||
                    transaction.category.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(state = scrollState)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Transaction list
        TransactionList(
            transactions = filteredTransactions,
            onTransactionClick = { transaction ->
                // TODO: Navigate to transaction detail screen
                // or show bottom sheet with transaction details
            }
        )
    }
}

/**
 * Handle transaction click
 * TODO: Implement navigation to transaction detail or show bottom sheet
 */
private fun handleTransactionClick(transaction: Transaction) {
    // Implementation will be added when detail screen is created
}

