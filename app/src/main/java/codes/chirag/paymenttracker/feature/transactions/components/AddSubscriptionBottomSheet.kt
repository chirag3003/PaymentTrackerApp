package codes.chirag.paymenttracker.feature.transactions.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.BillingFrequency
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import java.util.Calendar

private val subscriptionCategories = listOf(
    "Subscription", "Entertainment", "Health", "Education",
    "Food", "Groceries", "Transport", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amount: String,
        frequency: BillingFrequency,
        nextDueDate: String,
        category: String,
        paymentMethod: PaymentMethod
    ) -> Unit
) {
    var name         by rememberSaveable { mutableStateOf("") }
    var amount       by rememberSaveable { mutableStateOf("") }
    var frequency    by rememberSaveable { mutableStateOf(BillingFrequency.MONTHLY) }
    var nextDueDate  by rememberSaveable { mutableStateOf(defaultNextDueDate()) }
    var category     by rememberSaveable { mutableStateOf(subscriptionCategories.first()) }
    var paymentMethod by rememberSaveable { mutableStateOf(PaymentMethod.CARD) }

    val isValid = name.isNotBlank() && amount.toDoubleOrNull() != null

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
                    text = "Add Subscription",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = OnSurfaceMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("e.g. Netflix, Spotify", color = OnSurfaceMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency
            SectionLabel("Frequency")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillingFrequency.entries.forEach { freq ->
                    val selected = frequency == freq
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) OrangePrimary else SurfaceL3)
                            .border(
                                width = 1.dp,
                                color = if (selected) OrangePrimary else BorderColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { frequency = freq }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = freq.displayName(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) OnPrimary else OnSurfaceMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next due date
            OutlinedTextField(
                value = nextDueDate,
                onValueChange = { nextDueDate = it },
                label = { Text("Next Due Date") },
                placeholder = { Text("e.g. Mar 10, 2026", color = OnSurfaceMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category
            SectionLabel("Category")
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subscriptionCategories) { cat ->
                    val selected = category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) OrangePrimary else SurfaceL3)
                            .border(
                                width = 1.dp,
                                color = if (selected) OrangePrimary else BorderColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { category = cat }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) OnPrimary else OnSurfaceMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment method
            SectionLabel("Payment Method")
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PaymentMethod.entries) { method ->
                    val selected = paymentMethod == method
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) OrangePrimary else SurfaceL3)
                            .border(
                                width = 1.dp,
                                color = if (selected) OrangePrimary else BorderColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { paymentMethod = method }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = method.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) OnPrimary else OnSurfaceMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    onSave(name.trim(), amount, frequency, nextDueDate, category, paymentMethod)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor   = OnPrimary,
                    disabledContainerColor = SurfaceL3
                )
            ) {
                Text("Save Subscription", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceMuted
    )
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor     = OnBackground,
    unfocusedTextColor   = OnBackground,
    focusedBorderColor   = OrangePrimary,
    unfocusedBorderColor = BorderColor,
    cursorColor          = OrangePrimary,
    focusedLabelColor    = OrangePrimary,
    unfocusedLabelColor  = OnSurfaceMuted,
    focusedContainerColor   = Background,
    unfocusedContainerColor = Background
)

private fun BillingFrequency.displayName() = when (this) {
    BillingFrequency.WEEKLY  -> "Weekly"
    BillingFrequency.MONTHLY -> "Monthly"
    BillingFrequency.YEARLY  -> "Yearly"
}

private fun defaultNextDueDate(): String {
    val cal = Calendar.getInstance().also { it.add(Calendar.MONTH, 1) }
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
}
