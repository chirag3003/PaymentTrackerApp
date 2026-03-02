package codes.chirag.paymenttracker.feature.transactions.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.feature.transactions.TransactionViewModel
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
import java.util.Calendar

private val editSheetCategories = listOf(
    "Food", "Transport", "Shopping", "Entertainment",
    "Groceries", "Subscription", "Health", "Education", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionBottomSheet(
    transactionId: String,
    sheetState: SheetState,
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var transaction by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(transactionId) {
        transaction = viewModel.getById(transactionId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceL1,
        modifier = modifier
    ) {
        val tx = transaction
        if (tx == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            EditTransactionSheetContent(
                transaction = tx,
                onSave = { updated ->
                    viewModel.update(updated)
                    onSaved()
                },
                onDelete = {
                    viewModel.delete(tx.id)
                    onDeleted()
                },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun EditTransactionSheetContent(
    transaction: Transaction,
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var title    by remember { mutableStateOf(transaction.title) }
    var amount   by remember { mutableStateOf(transaction.amount.toString()) }
    var type     by remember { mutableStateOf(transaction.type) }
    var category by remember { mutableStateOf(transaction.category) }
    var method   by remember { mutableStateOf(transaction.paymentMethod) }
    var date     by remember { mutableStateOf(transaction.date) }
    var notes    by remember { mutableStateOf(transaction.notes) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            date = "${months[month]} $day, $year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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

        // Type toggle
        EditSheetTypeToggle(selected = type, onSelect = { type = it })

        // Amount field
        EditSheetLabeledField("Amount (₹)") {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = { Text("0.00", color = OnSurfaceMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = editSheetTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Title field
        EditSheetLabeledField("Title") {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Starbucks", color = OnSurfaceMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = editSheetTextFieldColors()
            )
        }

        // Date field (tappable)
        EditSheetLabeledField("Date") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceL3)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .clickable { datePickerDialog.show() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (date.isBlank()) OnSurfaceMuted else OnBackground
                )
            }
        }

        // Category chips
        EditSheetLabeledField("Category") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(editSheetCategories) { cat ->
                    EditSheetSelectableChip(
                        label = cat,
                        selected = cat == category,
                        onSelect = { category = cat }
                    )
                }
            }
        }

        // Payment method chips
        EditSheetLabeledField("Payment Method") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PaymentMethod.entries) { m ->
                    EditSheetSelectableChip(
                        label = m.name,
                        selected = m == method,
                        onSelect = { method = m }
                    )
                }
            }
        }

        // Notes field
        EditSheetLabeledField("Notes (optional)") {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Add a note...", color = OnSurfaceMuted) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = editSheetTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Save button
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: transaction.amount
                onSave(
                    transaction.copy(
                        title         = title.ifBlank { transaction.title },
                        amount        = amt,
                        type          = type,
                        category      = category,
                        date          = date.ifBlank { transaction.date },
                        paymentMethod = method,
                        notes         = notes
                    )
                )
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
                text = "Save Changes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Delete button
        TextButton(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
        ) {
            Text(
                text = "Delete Transaction",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EditSheetTypeToggle(
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
        TransactionType.entries.forEach { t ->
            val isSelected = t == selected
            val bgColor = when {
                isSelected && t == TransactionType.EXPENSE -> OrangePrimary
                isSelected && t == TransactionType.INCOME  -> IncomeGreen
                else -> Background
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onSelect(t) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = t.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) OnPrimary else OnSurfaceMuted
                )
            }
        }
    }
}

@Composable
private fun EditSheetSelectableChip(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val bg = if (selected) OrangePrimary else SurfaceL3
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

@Composable
private fun EditSheetLabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)
        content()
    }
}

@Composable
private fun editSheetTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OnBackground,
    unfocusedTextColor = OnBackground,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = BorderColor,
    cursorColor = OrangePrimary,
    focusedContainerColor = SurfaceL3,
    unfocusedContainerColor = SurfaceL3
)
