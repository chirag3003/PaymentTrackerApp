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
import codes.chirag.paymenttracker.feature.transactions.components.SearchBar
import codes.chirag.paymenttracker.feature.transactions.components.TransactionList
import codes.chirag.paymenttracker.feature.transactions.utils.getSampleTransactions

/**
 * Transactions screen displaying all user transactions with search functionality
 *
 * @param onTransactionClick Callback when a transaction is clicked with transaction ID
 * @param modifier Optional modifier for styling
 */
@Composable
fun TransactionsScreen(
    onTransactionClick: (String) -> Unit = {},
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
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar - fixed at top
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Transaction list - scrollable
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(state = scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TransactionList(
                transactions = filteredTransactions,
                onTransactionClick = { transaction ->
                    onTransactionClick(transaction.id)
                }
            )
        }
    }
}

