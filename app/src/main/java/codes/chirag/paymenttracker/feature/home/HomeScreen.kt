package codes.chirag.paymenttracker.feature.home

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import codes.chirag.paymenttracker.feature.home.components.CategoryBudgetSection
import codes.chirag.paymenttracker.feature.home.components.DailyBudgetWidget
import codes.chirag.paymenttracker.feature.home.components.HeroBalanceCard
import codes.chirag.paymenttracker.feature.home.components.HomeTopBar
import codes.chirag.paymenttracker.feature.home.components.RecentTransactionsSection
import codes.chirag.paymenttracker.feature.home.components.SpendingBarChart
import codes.chirag.paymenttracker.navigation.Route
import codes.chirag.paymenttracker.navigation.TransactionsRoute

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            HomeTopBar(
                userName = state.userName,
                onNotificationsClick = {}
            )
        }
        item {
            HeroBalanceCard(
                balance = state.balance,
                monthlyIncome = state.monthlyIncome,
                monthlyExpense = state.monthlyExpense,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            DailyBudgetWidget(
                safeToSpend = state.safeToSpend,
                dailyBudget = state.dailyBudget,
                spentToday  = state.spentToday,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            CategoryBudgetSection(
                categories = state.categorySpending
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            SpendingBarChart(
                weeklyData = state.weeklySpending,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            RecentTransactionsSection(
                transactions = state.recentTransactions,
                onSeeAllClick = {
                    navController.navigate(Route.TransactionsGraph) {
                        launchSingleTop = true
                    }
                },
                onTransactionClick = { id ->
                    // Switch to Transactions graph first so the tab becomes active,
                    // then push the detail screen on top of it.
                    navController.navigate(Route.TransactionsGraph) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    navController.navigate(TransactionsRoute.TransactionDetails(id))
                }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
