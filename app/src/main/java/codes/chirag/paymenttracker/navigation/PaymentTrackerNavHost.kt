package codes.chirag.paymenttracker.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import codes.chirag.paymenttracker.feature.analysis.AnalysisScreen
import codes.chirag.paymenttracker.feature.home.HomeScreen
import codes.chirag.paymenttracker.feature.settings.SettingsScreen
import codes.chirag.paymenttracker.feature.transactions.TransactionsScreen

/**
 * Main navigation host with nested graphs for each bottom nav item
 */
@Composable
fun PaymentTrackerNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.HomeGraph,
        modifier = modifier
    ) {
        homeGraph(innerPadding, navController)
        transactionsGraph(innerPadding)
        analysisGraph(innerPadding)
        settingsGraph(innerPadding)
    }
}

/**
 * Home feature navigation graph
 */
private fun NavGraphBuilder.homeGraph(innerPadding: PaddingValues, navController: NavHostController) {
    navigation<Route.HomeGraph>(
        startDestination = HomeRoute.Home
    ) {
        composable<HomeRoute.Home> {
            HomeScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
    }
}

/**
 * Transactions feature navigation graph
 */
private fun NavGraphBuilder.transactionsGraph(innerPadding: PaddingValues) {
    navigation<Route.TransactionsGraph>(
        startDestination = TransactionsRoute.TransactionsList
    ) {
        composable<TransactionsRoute.TransactionsList> {
            TransactionsScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Analysis feature navigation graph
 */
private fun NavGraphBuilder.analysisGraph(innerPadding: PaddingValues) {
    navigation<Route.AnalysisGraph>(
        startDestination = AnalysisRoute.AnalysisHome
    ) {
        composable<AnalysisRoute.AnalysisHome> {
            AnalysisScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Settings feature navigation graph
 */
private fun NavGraphBuilder.settingsGraph(innerPadding: PaddingValues) {
    navigation<Route.SettingsGraph>(
        startDestination = SettingsRoute.SettingsHome
    ) {
        composable<SettingsRoute.SettingsHome> {
            SettingsScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

