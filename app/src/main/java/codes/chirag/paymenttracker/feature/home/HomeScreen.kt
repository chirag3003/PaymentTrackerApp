package codes.chirag.paymenttracker.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codes.chirag.paymenttracker.ui.theme.AccentGreen
import codes.chirag.paymenttracker.ui.theme.AccentGreenLight
import codes.chirag.paymenttracker.ui.theme.AccentRed
import codes.chirag.paymenttracker.ui.theme.AccentRedLight
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp)
    ) {
        GreetingSection()
        TotalBalanceCard(
            totalBalance = 1234.56,
            income = 2500.00,
            expenses = 1265.44
        )
    }
}

@Composable
private fun GreetingSection(
    userName: String = "Chirag"
) {
    Text(
        text = "Welcome back, $userName!",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun TotalBalanceCard(
    totalBalance: Double,
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Balance Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Balance",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(totalBalance),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Income and Expenses Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Income
                BalanceItem(
                    label = "Income",
                    amount = income,
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )

                // Expenses
                BalanceItem(
                    label = "Expenses",
                    amount = expenses,
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    label: String,
    amount: Double,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isIncome) {
        if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            AccentGreenLight
        } else {
            AccentGreen
        }
    } else {
        if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            AccentRedLight
        } else {
            AccentRed
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formatCurrency(amount),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            letterSpacing = (-0.3).sp
        )
    }
}

private fun formatCurrency(amount: Double): String {
    return "₹ $amount"
}

// Extension function to calculate luminance
private fun androidx.compose.ui.graphics.Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

