package codes.chirag.paymenttracker.feature.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import codes.chirag.paymenttracker.feature.home.components.CategoryBudgetSection
import codes.chirag.paymenttracker.feature.home.components.DailyBudgetWidget
import codes.chirag.paymenttracker.feature.home.components.HeroBalanceCard
import codes.chirag.paymenttracker.feature.home.components.HomeTopBar
import codes.chirag.paymenttracker.feature.home.components.InsightsCard
import codes.chirag.paymenttracker.feature.home.components.RecentTransactionsSection
import codes.chirag.paymenttracker.feature.home.components.SpendingAlertsSheet
import codes.chirag.paymenttracker.feature.home.components.SpendingBarChart
import codes.chirag.paymenttracker.navigation.HomeRoute
import codes.chirag.paymenttracker.navigation.Route
import codes.chirag.paymenttracker.navigation.TransactionsRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showAlertsSheet by rememberSaveable { mutableStateOf(false) }
    val alertsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            HomeTopBar(
                userName = state.userName,
                onNotificationsClick = { showAlertsSheet = true }
            )
        }
        item {
            HeroBalanceCard(
                balance = state.balance,
                monthlyIncome = state.monthlyIncome,
                monthlyExpense = state.monthlyExpense,
                balancePeriod = state.balancePeriod,
                onPeriodChange = { viewModel.setBalancePeriod(it) },
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
                weeklyData  = state.weeklySpending,
                weekLabel   = state.homeWeekLabel,
                onPrevWeek  = { viewModel.homeWeekPrev() },
                onNextWeek  = { viewModel.homeWeekNext() },
                canGoNext   = state.homeWeekOffset < 0,
                modifier    = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            InsightsCard(
                state = state,
                onSeeFullReport = {
                    navController.navigate(HomeRoute.Insights)
                },
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
                    navController.navigate(Route.TransactionsGraph) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    navController.navigate(TransactionsRoute.TransactionDetails(id))
                }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showAlertsSheet) {
        SpendingAlertsSheet(
            state = state,
            sheetState = alertsSheetState,
            onDismiss = {
                scope.launch { alertsSheetState.hide() }.invokeOnCompletion {
                    showAlertsSheet = false
                }
            }
        )
    }
}
