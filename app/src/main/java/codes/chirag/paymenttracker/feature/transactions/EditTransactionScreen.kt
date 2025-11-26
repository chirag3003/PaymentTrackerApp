package codes.chirag.paymenttracker.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codes.chirag.paymenttracker.feature.home.models.Transaction

/**
 * Edit Transaction Screen
 * Allows editing of transaction details
 *
 * @param transactionId The ID of the transaction to edit
 * @param onNavigateBack Callback when back button is pressed
 * @param onSave Callback when save button is pressed
 * @param onDelete Callback when delete button is pressed
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onSave: (Transaction) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: Replace with actual data from ViewModel/Repository
    val transaction = codes.chirag.paymenttracker.feature.transactions.utils.getSampleTransactions()
        .find { it.id == transactionId } ?: return

    val scrollState = rememberScrollState()

    // State for editable fields
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var category by remember { mutableStateOf(transaction.category) }
    var date by remember { mutableStateOf(transaction.date) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Amount Input Section
            AmountInputSection(
                amount = amount,
                onAmountChange = { amount = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Editable Fields
            EditableFieldsList(
                category = category,
                date = date,
                notes = notes,
                onCategoryClick = { /* TODO: Show category picker */ },
                onDateClick = { /* TODO: Show date picker */ },
                onAccountClick = { /* TODO: Show account picker */ },
                onNotesChange = { notes = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            ActionButtons(
                onSave = {
                    // TODO: Create updated transaction and save
                    onNavigateBack()
                },
                onDelete = {
                    onDelete()
                    onNavigateBack()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Amount input section with dollar sign
 */
@Composable
private fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Amount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Box(modifier = modifier.fillMaxWidth()){
            Text(
                text = "₹",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 48.sp,
                modifier=modifier.align(Alignment.CenterStart)
            )


            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

    }
}

/**
 * List of editable fields
 */
@Composable
private fun EditableFieldsList(
    category: String,
    date: String,
    notes: String,
    onCategoryClick: () -> Unit,
    onDateClick: () -> Unit,
    onAccountClick: () -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category
        EditableFieldItem(
            icon = getCategoryIcon(category),
            iconColor = MaterialTheme.colorScheme.primary,
            label = "Category",
            value = category,
            onClick = onCategoryClick
        )

        // Date
        EditableFieldItem(
            icon = Icons.Default.CalendarToday,
            iconColor = MaterialTheme.colorScheme.primary,
            label = "Date",
            value = date,
            onClick = onDateClick
        )

        // Notes
        NotesField(
            notes = notes,
            onNotesChange = onNotesChange
        )
    }
}

/**
 * Individual editable field item (clickable)
 */
@Composable
private fun EditableFieldItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
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
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Edit",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Notes text field
 */
@Composable
private fun NotesField(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Notes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = {
                Text(
                    text = "Add notes...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            maxLines = 5
        )
    }
}

/**
 * Save and Delete action buttons
 */
@Composable
private fun ActionButtons(
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Save Changes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Delete Button
        TextButton(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color(0xFFEF5350)
            )
        ) {
            Text(
                text = "Delete Transaction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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

