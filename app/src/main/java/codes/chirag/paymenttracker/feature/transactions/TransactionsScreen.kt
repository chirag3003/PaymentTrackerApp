package codes.chirag.paymenttracker.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.utils.formatCurrency
import codes.chirag.paymenttracker.feature.transactions.components.AddTransactionBottomSheet
import codes.chirag.paymenttracker.feature.transactions.components.TransactionListItem
import codes.chirag.paymenttracker.feature.transactions.utils.getSampleTransactions
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.ExpenseRed
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import kotlinx.coroutines.launch

private enum class DateFilter(val label: String) {
    ALL("All"), TODAY("Today"), WEEK("This Week"), MONTH("This Month")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onTransactionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allTransactions = remember { getSampleTransactions() }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(DateFilter.ALL) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val filtered by remember(searchQuery, selectedFilter, allTransactions) {
        derivedStateOf {
            allTransactions.filter { tx ->
                val matchesSearch = searchQuery.isBlank() ||
                    tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (selectedFilter) {
                    DateFilter.ALL   -> true
                    DateFilter.TODAY -> tx.date == "Today"
                    DateFilter.WEEK  -> tx.date in listOf("Today", "Yesterday", "Feb 26, 2026", "Feb 25, 2026")
                    DateFilter.MONTH -> true // all sample data is from this month
                }
                matchesSearch && matchesFilter
            }
        }
    }

    // Group transactions by date
    val grouped by remember(filtered) {
        derivedStateOf { filtered.groupBy { it.date } }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OrangePrimary,
                contentColor = OnPrimary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search transactions...", color = OnSurfaceMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = OnSurfaceMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground,
                        unfocusedTextColor = OnBackground,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = OrangePrimary,
                        focusedContainerColor = SurfaceL3,
                        unfocusedContainerColor = SurfaceL3
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DateFilter.entries) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimary,
                                selectedLabelColor = OnPrimary,
                                containerColor = SurfaceL3,
                                labelColor = OnSurfaceMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = DividerColor,
                                selectedBorderColor = OrangePrimary
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            if (grouped.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceMuted
                        )
                    }
                }
            } else {
                grouped.forEach { (date, txList) ->
                    item(key = "header_$date") {
                        DateGroupHeader(
                            date = date,
                            transactions = txList,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item(key = "group_$date") {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceL1)
                        ) {
                            Column {
                                txList.forEachIndexed { index, tx ->
                                    TransactionListItem(
                                        transaction = tx,
                                        onClick = { onTransactionClick(tx.id) }
                                    )
                                    if (index < txList.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 74.dp),
                                            color = DividerColor,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionBottomSheet(
            sheetState = sheetState,
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showAddSheet = false
                }
            },
            onSave = { title, amount, type, category, paymentMethod, notes ->
                // TODO: persist via ViewModel
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showAddSheet = false
                }
            }
        )
    }
}

@Composable
private fun DateGroupHeader(
    date: String,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val dayTotal = transactions.sumOf { tx ->
        if (tx.type == codes.chirag.paymenttracker.core.model.TransactionType.INCOME)
            tx.amount else -tx.amount
    }
    val totalColor = if (dayTotal >= 0) IncomeGreen else ExpenseRed
    val prefix = if (dayTotal >= 0) "+" else ""
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceMuted
        )
        Text(
            text = "$prefix${formatCurrency(dayTotal)}",
            style = MaterialTheme.typography.labelMedium,
            color = totalColor
        )
    }
}
