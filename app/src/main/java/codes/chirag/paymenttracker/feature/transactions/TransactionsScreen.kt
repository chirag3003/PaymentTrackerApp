package codes.chirag.paymenttracker.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.Subscription
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.core.utils.formatCurrency
import codes.chirag.paymenttracker.feature.transactions.components.AddSubscriptionBottomSheet
import codes.chirag.paymenttracker.feature.transactions.components.SubscriptionListItem
import codes.chirag.paymenttracker.feature.transactions.components.TransactionListItem
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
import codes.chirag.paymenttracker.ui.theme.SurfaceL2
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import kotlinx.coroutines.launch
import java.util.Calendar

// All known categories across the app
private val knownCategories = listOf(
    "Food", "Dining", "Transport", "Shopping", "Entertainment",
    "Groceries", "Health", "Fitness", "Education",
    "Income", "Freelance", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    onTransactionClick: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val filtered by viewModel.filtered.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val dateRangeStart by viewModel.dateRangeStart.collectAsState()
    val dateRangeEnd by viewModel.dateRangeEnd.collectAsState()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsState()
    val subscriptions by subscriptionViewModel.subscriptions.collectAsState()

    // Group transactions by date
    val grouped = filtered.groupBy { it.date }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Add Subscription sheet state
    var showAddSubSheet by rememberSaveable { mutableStateOf(false) }
    var subscriptionToEdit by remember { mutableStateOf<Subscription?>(null) }
    val addSubSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Advanced filter sheet state
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        containerColor = Background,
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        subscriptionToEdit = null
                        showAddSubSheet = true
                    },
                    containerColor = OrangePrimary,
                    contentColor = OnPrimary
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Subscription")
                }
            }
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 80.dp,
                start = 0.dp,
                end = 0.dp
            )
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

            // Segment toggle: Transactions | Recurring
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Background,
                    contentColor = OrangePrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = OrangePrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceL2)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Transactions",
                                color = if (selectedTab == 0) OrangePrimary else OnSurfaceMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Recurring",
                                color = if (selectedTab == 1) OrangePrimary else OnSurfaceMuted
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Transactions tab ────────────────────────────────────────────
            if (selectedTab == 0) {
                // Search bar + filter button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
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
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            modifier = Modifier.weight(1f),
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

                        // Filter icon button with active badge
                        Box {
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (hasActiveFilters) OrangePrimary else SurfaceL3)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = "Advanced Filters",
                                    tint = if (hasActiveFilters) OnPrimary else OnSurfaceMuted
                                )
                            }
                            // Active dot badge
                            if (hasActiveFilters) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 6.dp, end = 6.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen)
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Date filter chips
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DateFilter.entries) { filter ->
                            FilterChip(
                                selected = activeFilter == filter,
                                onClick = { viewModel.activeFilter.value = filter },
                                label = { Text(filter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangePrimary,
                                    selectedLabelColor = OnPrimary,
                                    containerColor = SurfaceL3,
                                    labelColor = OnSurfaceMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = activeFilter == filter,
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

            // ── Recurring tab ───────────────────────────────────────────────
            if (selectedTab == 1) {
                if (subscriptions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No subscriptions yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap + to add a recurring bill",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceMuted
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceL1)
                        ) {
                            Column {
                                subscriptions.forEachIndexed { index, sub ->
                                    SubscriptionListItem(
                                        subscription = sub,
                                        onToggleActive = { subscriptionViewModel.toggleActive(it) },
                                        onDelete = { subscriptionViewModel.delete(it) },
                                        modifier = Modifier.clickable {
                                            subscriptionToEdit = sub
                                            showAddSubSheet = true
                                        }
                                    )
                                    if (index < subscriptions.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 68.dp),
                                            color = DividerColor,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add Subscription sheet ───────────────────────────────────────────────
    if (showAddSubSheet) {
        AddSubscriptionBottomSheet(
            sheetState = addSubSheetState,
            onDismiss = {
                scope.launch { addSubSheetState.hide() }.invokeOnCompletion {
                    showAddSubSheet = false
                }
            },
            onSave = { name, amount, type, frequency, nextDueDate, category, paymentMethod ->
                val editSub = subscriptionToEdit
                if (editSub != null) {
                    subscriptionViewModel.updateSubscription(
                        id = editSub.id,
                        name = name,
                        amount = amount,
                        type = type,
                        frequency = frequency,
                        nextDueDate = nextDueDate,
                        category = category,
                        paymentMethod = paymentMethod,
                        isActive = editSub.isActive
                    )
                } else {
                    subscriptionViewModel.add(name, amount, type, frequency, nextDueDate, category, paymentMethod)
                }
                scope.launch { addSubSheetState.hide() }.invokeOnCompletion {
                    showAddSubSheet = false
                }
            },
            initialSubscription = subscriptionToEdit
        )
    }

    // ── Advanced Filter sheet ────────────────────────────────────────────────
    if (showFilterSheet) {
        AdvancedFilterSheet(
            sheetState       = filterSheetState,
            currentType      = typeFilter,
            currentCategory  = categoryFilter,
            currentStartDate = dateRangeStart,
            currentEndDate   = dateRangeEnd,
            onApply          = { type, cat, start, end ->
                viewModel.typeFilter.value     = type
                viewModel.categoryFilter.value = cat
                viewModel.dateRangeStart.value = start
                viewModel.dateRangeEnd.value   = end
                scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                    showFilterSheet = false
                }
            },
            onClear = {
                viewModel.clearAdvancedFilters()
                scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                    showFilterSheet = false
                }
            },
            onDismiss = {
                scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                    showFilterSheet = false
                }
            }
        )
    }
}

// ── Advanced Filter Bottom Sheet ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AdvancedFilterSheet(
    sheetState: androidx.compose.material3.SheetState,
    currentType: TransactionType?,
    currentCategory: String?,
    currentStartDate: String?,
    currentEndDate: String?,
    onApply: (
        type: TransactionType?,
        category: String?,
        startDate: String?,
        endDate: String?
    ) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    // Local draft state inside the sheet
    var draftType     by rememberSaveable { mutableStateOf(currentType) }
    var draftCategory by rememberSaveable { mutableStateOf(currentCategory) }
    var draftStart    by rememberSaveable { mutableStateOf(currentStartDate) }
    var draftEnd      by rememberSaveable { mutableStateOf(currentEndDate) }

    // Date picker visibility
    var showStartPicker by rememberSaveable { mutableStateOf(false) }
    var showEndPicker   by rememberSaveable { mutableStateOf(false) }

    if (showStartPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        draftStart = millisToLabel(millis)
                    }
                    showStartPicker = false
                }) { Text("OK", color = OrangePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showEndPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        draftEnd = millisToLabel(millis)
                    }
                    showEndPicker = false
                }) { Text("OK", color = OrangePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        ) { DatePicker(state = pickerState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceL1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                TextButton(onClick = onClear) {
                    Text("Clear All", color = OrangePrimary, style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Type ──────────────────────────────────────────────────────
            FilterSectionLabel("Transaction Type")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // "All" chip
                TypeChip(
                    label = "All",
                    selected = draftType == null,
                    onClick = { draftType = null }
                )
                TypeChip(
                    label = "Income",
                    selected = draftType == TransactionType.INCOME,
                    onClick = { draftType = if (draftType == TransactionType.INCOME) null else TransactionType.INCOME }
                )
                TypeChip(
                    label = "Expense",
                    selected = draftType == TransactionType.EXPENSE,
                    onClick = { draftType = if (draftType == TransactionType.EXPENSE) null else TransactionType.EXPENSE }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Category ──────────────────────────────────────────────────
            FilterSectionLabel("Category")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                TypeChip(
                    label = "All",
                    selected = draftCategory == null,
                    onClick = { draftCategory = null }
                )
                knownCategories.forEach { cat ->
                    TypeChip(
                        label = cat,
                        selected = draftCategory == cat,
                        onClick = { draftCategory = if (draftCategory == cat) null else cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Date Range ────────────────────────────────────────────────
            FilterSectionLabel("Date Range")
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start date
                DateRangeField(
                    label = "From",
                    value = draftStart ?: "Any",
                    onClick = { showStartPicker = true },
                    onClear = { draftStart = null },
                    modifier = Modifier.weight(1f)
                )
                // End date
                DateRangeField(
                    label = "To",
                    value = draftEnd ?: "Any",
                    onClick = { showEndPicker = true },
                    onClear = { draftEnd = null },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Apply button
            Button(
                onClick = { onApply(draftType, draftCategory, draftStart, draftEnd) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor   = OnPrimary
                )
            ) {
                Text("Apply Filters", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = OnSurfaceMuted
    )
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) OrangePrimary else SurfaceL3)
            .border(
                width = 1.dp,
                color = if (selected) OrangePrimary else BorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) OnPrimary else OnSurfaceMuted
        )
    }
}

@Composable
private fun DateRangeField(
    label: String,
    value: String,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceL2)
                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = if (value == "Any") OnSurfaceMuted else OnBackground,
                modifier = Modifier.weight(1f)
            )
            if (value != "Any") {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(OnSurfaceMuted.copy(alpha = 0.2f))
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceMuted
                    )
                }
            }
        }
    }
}

// ── Date header component ────────────────────────────────────────────────────

@Composable
private fun DateGroupHeader(
    date: String,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val dayTotal = transactions.sumOf { tx ->
        if (tx.type == TransactionType.INCOME) tx.amount else -tx.amount
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

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun millisToLabel(millis: Long): String {
    val cal = Calendar.getInstance().also { it.timeInMillis = millis }
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
}
