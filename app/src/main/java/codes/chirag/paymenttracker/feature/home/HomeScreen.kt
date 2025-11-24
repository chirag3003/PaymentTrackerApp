package codes.chirag.paymenttracker.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.feature.home.components.GreetingSection
import codes.chirag.paymenttracker.feature.home.components.SpendingSection
import codes.chirag.paymenttracker.feature.home.components.TotalBalanceCard
import codes.chirag.paymenttracker.feature.home.models.CategorySpending
import codes.chirag.paymenttracker.feature.home.models.SpendingSummary

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Sample data for spending
    val spendingSummary = SpendingSummary(
        totalSpent = 850.00,
        lastMonthSpent = 800.00,
        categoryBreakdown = listOf(
            CategorySpending(
                category = "Food",
                amount = 320.00,
                maxAmount = 500.00,
                color = Color(0xFF4CAF50)
            ),
            CategorySpending(
                category = "Transport",
                amount = 150.00,
                maxAmount = 300.00,
                color = Color(0xFF2196F3)
            ),
            CategorySpending(
                category = "Shopping",
                amount = 280.00,
                maxAmount = 400.00,
                color = Color(0xFFFF9800)
            ),
            CategorySpending(
                category = "Entertainment",
                amount = 100.00,
                maxAmount = 200.00,
                color = Color(0xFF9C27B0)
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp)
    ) {
        GreetingSection()
        TotalBalanceCard(
            totalBalance = 1234.56,
            income = 2500.00,
            expenses = 1265.44
        )
        SpendingSection(
            spendingSummary = spendingSummary
        )
    }
}

