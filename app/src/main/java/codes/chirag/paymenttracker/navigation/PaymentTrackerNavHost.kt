package codes.chirag.paymenttracker.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import codes.chirag.paymenttracker.feature.goals.GoalsScreen
import codes.chirag.paymenttracker.feature.home.HomeScreen
import codes.chirag.paymenttracker.feature.onboarding.OnboardingScreen
import codes.chirag.paymenttracker.feature.settings.SettingsScreen
import codes.chirag.paymenttracker.feature.transactions.EditTransactionScreen
import codes.chirag.paymenttracker.feature.transactions.TransactionDetailsScreen
import codes.chirag.paymenttracker.feature.transactions.TransactionsScreen

/**
 * Main navigation host.
 *
 * @param showOnboarding  When true the graph starts at [OnboardingGraph] and
 *                        navigates to [Route.HomeGraph] once the user completes
 *                        onboarding. When false it starts directly at [Route.HomeGraph].
 * @param onOnboardingComplete  Called with (name, budget, method) so the host
 *                              can persist the flag and data.
 */
@Composable
fun PaymentTrackerNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    showOnboarding: Boolean,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val start: Any = if (showOnboarding) OnboardingGraph else Route.HomeGraph

    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier.navigationBarsPadding().statusBarsPadding()
    ) {
        // Onboarding graph (only reachable on first launch)
        navigation<OnboardingGraph>(
            startDestination = OnboardingRoute.Onboarding
        ) {
            composable<OnboardingRoute.Onboarding> {
                OnboardingScreen(
                    onComplete = { _, _, _ ->
                        onOnboardingComplete()
                        navController.navigate(Route.HomeGraph) {
                            popUpTo(OnboardingGraph) { inclusive = true }
                        }
                    }
                )
            }
        }

        homeGraph(innerPadding, navController)
        transactionsGraph(innerPadding, navController)
        goalsGraph(innerPadding)
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
private fun NavGraphBuilder.transactionsGraph(innerPadding: PaddingValues, navController: NavController) {
    navigation<Route.TransactionsGraph>(
        startDestination = TransactionsRoute.TransactionsList
    ) {
        composable<TransactionsRoute.TransactionsList> {
            TransactionsScreen(
                modifier = Modifier.padding(innerPadding),
                onTransactionClick = { transactionId ->
                    navController.navigate(TransactionsRoute.TransactionDetails(transactionId))
                }
            )
        }

        composable<TransactionsRoute.TransactionDetails> { backStackEntry ->
            val args = backStackEntry.toRoute<TransactionsRoute.TransactionDetails>()
            TransactionDetailsScreen(
                transactionId = args.transactionId,
                onNavigateBack = { navController.navigateUp() },
                onEdit = {
                    navController.navigate(TransactionsRoute.EditTransaction(args.transactionId))
                }
            )
        }

        composable<TransactionsRoute.EditTransaction> { backStackEntry ->
            val args = backStackEntry.toRoute<TransactionsRoute.EditTransaction>()
            EditTransactionScreen(
                transactionId = args.transactionId,
                onNavigateBack = { navController.navigateUp() },
                onSave = { _ ->
                    // TODO: Save transaction through ViewModel
                    navController.navigateUp()
                },
                onDelete = {
                    // TODO: Delete transaction through ViewModel
                    navController.popBackStack(
                        route = TransactionsRoute.TransactionsList,
                        inclusive = false
                    )
                }
            )
        }
    }
}

/**
 * Goals feature navigation graph
 */
private fun NavGraphBuilder.goalsGraph(innerPadding: PaddingValues) {
    navigation<Route.GoalsGraph>(
        startDestination = GoalsRoute.GoalsList
    ) {
        composable<GoalsRoute.GoalsList> {
            GoalsScreen(
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
