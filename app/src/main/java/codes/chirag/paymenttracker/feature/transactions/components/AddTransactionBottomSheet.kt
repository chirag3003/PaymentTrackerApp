package codes.chirag.paymenttracker.feature.transactions.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.BillingFrequency
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import java.util.Calendar

private val categories = listOf(
    "Food", "Transport", "Shopping", "Entertainment",
    "Groceries", "Living", "Health", "Education", "Other"
)

private val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

// Compute "next occurrence" of the given weekday (0=Sun..6=Sat) from today
private fun nextWeeklyDueDate(dayOfWeekIndex: Int): String {
    val cal = Calendar.getInstance()
    val todayDow = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun..6=Sat
    val daysAhead = (dayOfWeekIndex - todayDow + 7) % 7
    val targetCal = if (daysAhead == 0) cal else cal.also { it.add(Calendar.DAY_OF_MONTH, daysAhead) }
    return formatCalendarLabel(targetCal)
}

// Compute "next occurrence" of the given day-of-month (1..28) from today
private fun nextMonthlyDueDate(dayOfMonth: Int): String {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_MONTH)
    if (dayOfMonth < today) {
        // next month
        cal.add(Calendar.MONTH, 1)
    }
    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
    return formatCalendarLabel(cal)
}

private fun formatCalendarLabel(cal: Calendar): String {
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
}

private fun todayDisplayLabel(): String = formatCalendarLabel(Calendar.getInstance())
private fun yesterdayDisplayLabel(): String {
    val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -1) }
    return formatCalendarLabel(cal)
}

/** Converts a display date string to the label stored in Transaction.date */
private fun toTransactionDateLabel(displayDate: String): String {
    val today     = todayDisplayLabel()
    val yesterday = yesterdayDisplayLabel()
    return when (displayDate) {
        today     -> "Today"
        yesterday -> "Yesterday"
        else      -> displayDate
    }
}

data class RecurringInfo(
    val frequency: BillingFrequency,    // WEEKLY or MONTHLY
    val recurDayOfWeek: Int,            // 0=Sun..6=Sat (used when WEEKLY)
    val recurDayOfMonth: Int,           // 1..28 (used when MONTHLY)
    val nextDueDate: String             // pre-computed display string
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: String,
        type: TransactionType,
        category: String,
        paymentMethod: PaymentMethod,
        notes: String,
        date: String,
        recurring: RecurringInfo?
    ) -> Unit,
    modifier: Modifier = Modifier,
    initialTitle: String = "",
    initialAmount: String = "",
    initialType: TransactionType = TransactionType.EXPENSE,
    initialCategory: String = "Food",
    initialMethod: PaymentMethod = PaymentMethod.UPI,
    initialNotes: String = ""
) {
    val context = LocalContext.current
    val today = remember { Calendar.getInstance() }

    var title           by rememberSaveable { mutableStateOf(initialTitle) }
    var amount          by rememberSaveable { mutableStateOf(initialAmount) }
    var selectedType    by rememberSaveable { mutableStateOf(initialType) }
    var selectedCategory by rememberSaveable { mutableStateOf(initialCategory) }
    var selectedMethod  by rememberSaveable { mutableStateOf(initialMethod) }
    var notes           by rememberSaveable { mutableStateOf(initialNotes) }

    // Date picker state — default = today's display string
    var selectedDate by rememberSaveable { mutableStateOf(todayDisplayLabel()) }

    // Recurring toggle
    var isRecurring by rememberSaveable { mutableStateOf(false) }

    // Frequency: WEEKLY or MONTHLY (no YEARLY in this sheet)
    var recurFrequency by rememberSaveable { mutableStateOf(BillingFrequency.MONTHLY) }

    // Day-of-week default = today's weekday (0=Sun..6=Sat)
    val todayDow = remember { today.get(Calendar.DAY_OF_WEEK) - 1 }
    var recurDayOfWeek by rememberSaveable { mutableIntStateOf(todayDow) }

    // Day-of-month default = today's date clamped to 28
    val todayDom = remember { today.get(Calendar.DAY_OF_MONTH).coerceAtMost(28) }
    var recurDayOfMonth by rememberSaveable { mutableIntStateOf(todayDom) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceL1,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnBackground
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceL3)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Type toggle ───────────────────────────────────────────────────
            TypeToggle(selected = selectedType, onSelect = { selectedType = it })

            Spacer(modifier = Modifier.height(16.dp))

            // ── Title field ───────────────────────────────────────────────────
            LabeledField("Title") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Starbucks", color = OnSurfaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Amount field ──────────────────────────────────────────────────
            LabeledField("Amount (₹)") {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("0.00", color = OnSurfaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Date picker row ───────────────────────────────────────────────
            AnimatedVisibility(visible = !isRecurring, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    LabeledField("Date") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceL3)
                                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    val cal = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                                            selectedDate = "${months[month]} $day, $year"
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).apply {
                                        datePicker.maxDate = System.currentTimeMillis()
                                    }.show()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedDate == todayDisplayLabel()) "Today  ($selectedDate)" else selectedDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnBackground
                            )
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Pick date",
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Category chips ────────────────────────────────────────────────
            LabeledField("Category") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        SelectableChip(
                            label = cat,
                            selected = cat == selectedCategory,
                            onSelect = { selectedCategory = cat }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Payment method chips ──────────────────────────────────────────
            LabeledField("Payment Method") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaymentMethod.entries) { method ->
                        SelectableChip(
                            label = method.name,
                            selected = method == selectedMethod,
                            onSelect = { selectedMethod = method }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Notes field ───────────────────────────────────────────────────
            LabeledField("Notes (optional)") {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add a note...", color = OnSurfaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Recurring toggle row ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceL3)
                    .border(0.5.dp, if (isRecurring) OrangePrimary else DividerColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Repeat,
                        contentDescription = null,
                        tint = if (isRecurring) OrangePrimary else OnSurfaceMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Make Recurring",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackground
                        )
                        Text(
                            text = "Also create a subscription entry",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnPrimary,
                        checkedTrackColor = OrangePrimary,
                        uncheckedThumbColor = OnSurfaceMuted,
                        uncheckedTrackColor = Background
                    )
                )
            }

            // ── Recurring expansion ───────────────────────────────────────────
            AnimatedVisibility(
                visible = isRecurring,
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeSubtle)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Frequency: Weekly / Monthly
                    LabeledField("Frequency") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(BillingFrequency.WEEKLY, BillingFrequency.MONTHLY).forEach { freq ->
                                SelectableChip(
                                    label = freq.name.lowercase().replaceFirstChar { it.uppercase() },
                                    selected = recurFrequency == freq,
                                    onSelect = { recurFrequency = freq }
                                )
                            }
                        }
                    }

                    // Day selector (conditional on frequency)
                    if (recurFrequency == BillingFrequency.WEEKLY) {
                        LabeledField("Repeats on") {
                            WeekdaySelector(
                                selected = recurDayOfWeek,
                                onSelect = { recurDayOfWeek = it }
                            )
                        }
                    } else {
                        LabeledField("Repeats on day") {
                            MonthDaySelector(
                                selected = recurDayOfMonth,
                                onSelect = { recurDayOfMonth = it }
                            )
                        }
                    }

                    // Next due date preview
                    val nextDue = if (recurFrequency == BillingFrequency.WEEKLY)
                        nextWeeklyDueDate(recurDayOfWeek)
                    else
                        nextMonthlyDueDate(recurDayOfMonth)

                    Text(
                        text = "Next due: $nextDue",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrangePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    if (title.isNotBlank() && amount.isNotBlank()) {
                        val recurringInfo = if (isRecurring) {
                            val nextDue = if (recurFrequency == BillingFrequency.WEEKLY)
                                nextWeeklyDueDate(recurDayOfWeek)
                            else
                                nextMonthlyDueDate(recurDayOfMonth)
                            RecurringInfo(
                                frequency       = recurFrequency,
                                recurDayOfWeek  = recurDayOfWeek,
                                recurDayOfMonth = recurDayOfMonth,
                                nextDueDate     = nextDue
                            )
                        } else null

                        onSave(
                            title,
                            amount,
                            selectedType,
                            selectedCategory,
                            selectedMethod,
                            notes,
                            toTransactionDateLabel(selectedDate),
                            recurringInfo
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = OnPrimary
                )
            ) {
                Text(
                    text = if (isRecurring) "Save & Add Subscription" else "Save Transaction",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

// ── Weekday selector (Sun–Sat chips) ─────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekdaySelector(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        daysOfWeek.forEachIndexed { index, day ->
            SelectableChip(
                label = day,
                selected = index == selected,
                onSelect = { onSelect(index) }
            )
        }
    }
}

// ── Month-day selector (1–28) ─────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthDaySelector(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..28).forEach { day ->
            val isSelected = day == selected
            val bg        = if (isSelected) OrangePrimary else SurfaceL1
            val textColor = if (isSelected) OnPrimary else OnSurfaceMuted
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .then(if (!isSelected) Modifier.border(0.5.dp, DividerColor, RoundedCornerShape(8.dp)) else Modifier)
                    .clickable { onSelect(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )
            }
        }
    }
}

// ── TypeToggle ────────────────────────────────────────────────────────────────

@Composable
private fun TypeToggle(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = type == selected
            val bgColor = when {
                isSelected && type == TransactionType.EXPENSE -> OrangePrimary
                isSelected && type == TransactionType.INCOME  -> IncomeGreen
                else -> Background
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onSelect(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) OnPrimary else OnSurfaceMuted
                )
            }
        }
    }
}

// ── SelectableChip ────────────────────────────────────────────────────────────

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val bg        = if (selected) OrangePrimary else SurfaceL3
    val textColor = if (selected) OnPrimary else OnSurfaceMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .then(if (!selected) Modifier.border(0.5.dp, DividerColor, RoundedCornerShape(20.dp)) else Modifier)
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

// ── LabeledField ──────────────────────────────────────────────────────────────

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceMuted
        )
        content()
    }
}

// ── TextFieldColors ───────────────────────────────────────────────────────────

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OnBackground,
    unfocusedTextColor = OnBackground,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = BorderColor,
    cursorColor = OrangePrimary,
    focusedContainerColor = SurfaceL3,
    unfocusedContainerColor = SurfaceL3
)
