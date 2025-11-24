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
import androidx.navigation.NavHostController
import codes.chirag.paymenttracker.feature.home.components.GreetingSection
import codes.chirag.paymenttracker.feature.home.components.RecentTransactionsSection
import codes.chirag.paymenttracker.feature.home.components.SpendingSection
import codes.chirag.paymenttracker.feature.home.components.TotalBalanceCard
import codes.chirag.paymenttracker.feature.home.models.CategorySpending
import codes.chirag.paymenttracker.feature.home.models.SpendingSummary
import codes.chirag.paymenttracker.feature.home.models.Transaction
import codes.chirag.paymenttracker.feature.home.models.TransactionType
import codes.chirag.paymenttracker.navigation.BottomNavDestination

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
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

    // Sample data for recent transactions
    val recentTransactions = listOf(
        Transaction(
            id = "1",
            title = "Netflix Subscription",
            amount = 15.99,
            type = TransactionType.EXPENSE,
            category = "Subscription",
            date = "Oct 28, 2023",
            color = Color(0xFFE53935)
        ),
        Transaction(
            id = "2",
            title = "Monthly Salary",
            amount = 2500.00,
            type = TransactionType.INCOME,
            category = "Salary",
            date = "Oct 27, 2023",
            color = Color(0xFF4CAF50)
        ),
        Transaction(
            id = "3",
            title = "Starbucks",
            amount = 5.50,
            type = TransactionType.EXPENSE,
            category = "Food",
            date = "Oct 26, 2023",
            color = Color(0xFFFF9800)
        ),
        Transaction(
            id = "4",
            title = "Uber",
            amount = 12.30,
            type = TransactionType.EXPENSE,
            category = "Transport",
            date = "Oct 25, 2023",
            color = Color(0xFF2196F3)
        ),
        Transaction(
            id = "5",
            title = "Amazon Purchase",
            amount = 45.99,
            type = TransactionType.EXPENSE,
            category = "Shopping",
            date = "Oct 24, 2023",
            color = Color(0xFF9C27B0)
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
        RecentTransactionsSection(
            transactions = recentTransactions,
            onSeeAllClick = {
                navController.navigate(BottomNavDestination.TRANSACTIONS.route){
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            }
        )
    }
}

