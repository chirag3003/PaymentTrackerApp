package codes.chirag.paymenttracker.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import codes.chirag.paymenttracker.core.data.repository.GoalRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.feature.goals.GoalDetailScreen
import codes.chirag.paymenttracker.feature.goals.GoalViewModel
import codes.chirag.paymenttracker.feature.goals.GoalsScreen
import codes.chirag.paymenttracker.feature.home.HomeScreen
import codes.chirag.paymenttracker.feature.home.HomeViewModel
import codes.chirag.paymenttracker.feature.onboarding.OnboardingScreen
import codes.chirag.paymenttracker.feature.settings.SettingsScreen
import codes.chirag.paymenttracker.feature.settings.SettingsViewModel
import codes.chirag.paymenttracker.feature.transactions.EditTransactionScreen
import codes.chirag.paymenttracker.feature.transactions.TransactionDetailsScreen
import codes.chirag.paymenttracker.feature.transactions.TransactionViewModel
import codes.chirag.paymenttracker.feature.transactions.TransactionsScreen

/**
 * Main navigation host.
 *
 * @param showOnboarding  When true the graph starts at [OnboardingGraph].
 * @param onOnboardingComplete  Called with (name, budget, method) so the host
 *                              can persist the flag and user data.
 */
@Composable
fun PaymentTrackerNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    showOnboarding: Boolean,
    onOnboardingComplete: (name: String, budget: String, method: PaymentMethod) -> Unit,
    txRepo: TransactionRepository,
    goalRepo: GoalRepository,
    profileRepo: UserProfileRepository,
    modifier: Modifier = Modifier
) {
    val start: Any = if (showOnboarding) OnboardingGraph else Route.HomeGraph

    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier
    ) {
        // Onboarding graph (only reachable on first launch)
        navigation<OnboardingGraph>(
            startDestination = OnboardingRoute.Onboarding
        ) {
            composable<OnboardingRoute.Onboarding> {
                OnboardingScreen(
                    onComplete = { name, budget, method ->
                        onOnboardingComplete(name, budget.toString(), method)
                        navController.navigate(Route.HomeGraph) {
                            popUpTo(OnboardingGraph) { inclusive = true }
                        }
                    }
                )
            }
        }

        homeGraph(innerPadding, navController, txRepo, profileRepo)
        transactionsGraph(innerPadding, navController, txRepo)
        goalsGraph(innerPadding, navController, goalRepo)
        settingsGraph(innerPadding, navController, profileRepo, txRepo)
    }
}

/**
 * Home feature navigation graph
 */
private fun NavGraphBuilder.homeGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    txRepo: TransactionRepository,
    profileRepo: UserProfileRepository
) {
    navigation<Route.HomeGraph>(
        startDestination = HomeRoute.Home
    ) {
        composable<HomeRoute.Home> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.HomeGraph)
            }
            val homeViewModel: HomeViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = HomeViewModel.factory(txRepo, profileRepo)
            )
            HomeScreen(
                viewModel = homeViewModel,
                contentPadding = innerPadding,
                navController = navController
            )
        }
    }
}

/**
 * Transactions feature navigation graph — one TransactionViewModel scoped to the graph.
 */
private fun NavGraphBuilder.transactionsGraph(
    innerPadding: PaddingValues,
    navController: NavController,
    txRepo: TransactionRepository
) {
    navigation<Route.TransactionsGraph>(
        startDestination = TransactionsRoute.TransactionsList
    ) {
        composable<TransactionsRoute.TransactionsList> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.TransactionsGraph)
            }
            val txViewModel: TransactionViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = TransactionViewModel.factory(txRepo)
            )
            TransactionsScreen(
                viewModel = txViewModel,
                contentPadding = innerPadding,
                onTransactionClick = { transactionId ->
                    navController.navigate(TransactionsRoute.TransactionDetails(transactionId))
                }
            )
        }

        composable<TransactionsRoute.TransactionDetails> { backStackEntry ->
            val args = backStackEntry.toRoute<TransactionsRoute.TransactionDetails>()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.TransactionsGraph)
            }
            val txViewModel: TransactionViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = TransactionViewModel.factory(txRepo)
            )
            TransactionDetailsScreen(
                transactionId = args.transactionId,
                viewModel = txViewModel,
                onNavigateBack = { navController.navigateUp() },
                onEdit = {
                    navController.navigate(TransactionsRoute.EditTransaction(args.transactionId))
                },
                modifier = Modifier.padding(innerPadding)
            )
        }

        composable<TransactionsRoute.EditTransaction> { backStackEntry ->
            val args = backStackEntry.toRoute<TransactionsRoute.EditTransaction>()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.TransactionsGraph)
            }
            val txViewModel: TransactionViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = TransactionViewModel.factory(txRepo)
            )
            EditTransactionScreen(
                transactionId = args.transactionId,
                viewModel = txViewModel,
                onNavigateBack = { navController.navigateUp() },
                onSaveAndNavigateBack = { navController.navigateUp() },
                onDeleteAndNavigateToList = {
                    navController.popBackStack(
                        route = TransactionsRoute.TransactionsList,
                        inclusive = false
                    )
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Goals feature navigation graph — one GoalViewModel scoped to the graph.
 */
private fun NavGraphBuilder.goalsGraph(
    innerPadding: PaddingValues,
    navController: NavController,
    goalRepo: GoalRepository
) {
    navigation<Route.GoalsGraph>(
        startDestination = GoalsRoute.GoalsList
    ) {
        composable<GoalsRoute.GoalsList> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.GoalsGraph)
            }
            val goalViewModel: GoalViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = GoalViewModel.factory(goalRepo)
            )
            GoalsScreen(
                viewModel = goalViewModel,
                contentPadding = innerPadding,
                onGoalClick = { goalId ->
                    navController.navigate(GoalsRoute.GoalDetail(goalId))
                }
            )
        }
        composable<GoalsRoute.GoalDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<GoalsRoute.GoalDetail>()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.GoalsGraph)
            }
            val goalViewModel: GoalViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = GoalViewModel.factory(goalRepo)
            )
            GoalDetailScreen(
                goalId = args.goalId,
                viewModel = goalViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

/**
 * Settings feature navigation graph
 */
private fun NavGraphBuilder.settingsGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    profileRepo: UserProfileRepository,
    txRepo: TransactionRepository
) {
    navigation<Route.SettingsGraph>(
        startDestination = SettingsRoute.SettingsHome
    ) {
        composable<SettingsRoute.SettingsHome> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.SettingsGraph)
            }
            val context = androidx.compose.ui.platform.LocalContext.current
            val settingsViewModel: SettingsViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = SettingsViewModel.factory(profileRepo, txRepo, context)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// Helper: remember a back-stack entry in the context of a composable.
// Defined as an extension so it can be called inside NavGraphBuilder lambdas.
@Composable
private fun <T> remember(key: T, calculation: () -> androidx.navigation.NavBackStackEntry) =
    androidx.compose.runtime.remember(key) { calculation() }
