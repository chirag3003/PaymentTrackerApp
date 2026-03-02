package codes.chirag.paymenttracker.feature.home.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.BalancePeriod
import codes.chirag.paymenttracker.core.utils.formatCurrency
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.ExpenseRed
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL2
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroBalanceCard(
    balance: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    balancePeriod: BalancePeriod = BalancePeriod.Monthly,
    onPeriodChange: (BalancePeriod) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // DatePickerDialog for FromDate selection
    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val cal = Calendar.getInstance().also { it.timeInMillis = millis }
                        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                        val label = "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
                        onPeriodChange(BalancePeriod.FromDate(label))
                    }
                    showDatePicker = false
                }) { Text("OK", color = OrangePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2A1500),
                        Color(0xFF1A0D00)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            // ── Period selector ──────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodChip(
                    label = "Monthly",
                    selected = balancePeriod is BalancePeriod.Monthly,
                    onClick = { onPeriodChange(BalancePeriod.Monthly) }
                )
                PeriodChip(
                    label = "All Time",
                    selected = balancePeriod is BalancePeriod.AllTime,
                    onClick = { onPeriodChange(BalancePeriod.AllTime) }
                )
                PeriodChip(
                    label = when (balancePeriod) {
                        is BalancePeriod.FromDate -> "Since ${balancePeriod.startLabel}"
                        else -> "From Date"
                    },
                    selected = balancePeriod is BalancePeriod.FromDate,
                    onClick = { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Balance label ────────────────────────────────────────────
            Text(
                text = when (balancePeriod) {
                    is BalancePeriod.Monthly  -> "This Month's Balance"
                    is BalancePeriod.AllTime  -> "All-time Balance"
                    is BalancePeriod.FromDate -> "Balance Since ${balancePeriod.startLabel}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.displaySmall,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IncomeExpenseChip(
                    label = "Income",
                    amount = monthlyIncome,
                    icon = Icons.Outlined.ArrowDownward,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                IncomeExpenseChip(
                    label = "Expenses",
                    amount = monthlyExpense,
                    icon = Icons.Outlined.ArrowUpward,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        // Decorative accent dot
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(80.dp)
                .clip(CircleShape)
                .background(OrangeSubtle)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(OrangePrimary.copy(alpha = 0.15f))
        )
    }
}

@Composable
private fun PeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) OrangePrimary else SurfaceL2)
            .border(
                width = 1.dp,
                color = if (selected) OrangePrimary else BorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) OnPrimary else OnSurfaceMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun IncomeExpenseChip(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleSmall,
                color = OnBackground
            )
        }
    }
}
