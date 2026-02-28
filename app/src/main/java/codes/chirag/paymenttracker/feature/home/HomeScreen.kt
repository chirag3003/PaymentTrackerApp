package codes.chirag.paymenttracker.feature.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import codes.chirag.paymenttracker.core.model.CategorySpending
import codes.chirag.paymenttracker.feature.home.components.CategoryBudgetSection
import codes.chirag.paymenttracker.feature.home.components.DailyBudgetWidget
import codes.chirag.paymenttracker.feature.home.components.HeroBalanceCard
import codes.chirag.paymenttracker.feature.home.components.HomeTopBar
import codes.chirag.paymenttracker.feature.home.components.RecentTransactionsSection
import codes.chirag.paymenttracker.feature.home.components.SpendingBarChart
import codes.chirag.paymenttracker.feature.home.components.WeeklyBarData
import codes.chirag.paymenttracker.feature.transactions.utils.getSampleTransactions
import codes.chirag.paymenttracker.navigation.Route

// ── Sample data (UI prototype; replaced by ViewModel in data-layer phase) ──────
private val sampleCategorySpending = listOf(
    CategorySpending(category = "Food",          amount = 2840.0,  budget = 4000.0),
    CategorySpending(category = "Transport",     amount = 1200.0,  budget = 2000.0),
    CategorySpending(category = "Shopping",      amount = 3800.0,  budget = 3000.0), // over budget
    CategorySpending(category = "Entertainment", amount = 1100.0,  budget = 2000.0),
    CategorySpending(category = "Groceries",     amount = 2200.0,  budget = 3000.0),
    CategorySpending(category = "Subscription",  amount = 768.0,   budget = 1000.0)
)

private val sampleWeeklyData = listOf(
    WeeklyBarData("Mon", 540.0),
    WeeklyBarData("Tue", 1200.0),
    WeeklyBarData("Wed", 320.0),
    WeeklyBarData("Thu", 890.0),
    WeeklyBarData("Fri", 2100.0),
    WeeklyBarData("Sat", 1480.0),
    WeeklyBarData("Sun", 380.0)
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val transactions = getSampleTransactions().take(5)

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            HomeTopBar(
                userName = "Chirag",
                onNotificationsClick = {}
            )
        }
        item {
            HeroBalanceCard(
                balance = 42_350.0,
                monthlyIncome = 23_000.0,
                monthlyExpense = 11_796.0,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            DailyBudgetWidget(
                safeToSpend = 620.0,
                dailyBudget = 1000.0,
                spentToday = 380.0,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            CategoryBudgetSection(
                categories = sampleCategorySpending
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            SpendingBarChart(
                weeklyData = sampleWeeklyData,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            RecentTransactionsSection(
                transactions = transactions,
                onSeeAllClick = {
                    navController.navigate(Route.TransactionsGraph) {
                        launchSingleTop = true
                    }
                },
                onTransactionClick = { /* no-op for now */ }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
