package codes.chirag.paymenttracker.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import codes.chirag.paymenttracker.core.data.repository.PreferencesRepository
import codes.chirag.paymenttracker.core.data.repository.SubscriptionRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.feature.goals.GoalDetailScreen
import codes.chirag.paymenttracker.feature.goals.GoalViewModel
import codes.chirag.paymenttracker.feature.goals.GoalsScreen
import codes.chirag.paymenttracker.feature.home.HomeScreen
import codes.chirag.paymenttracker.feature.home.HomeViewModel
import codes.chirag.paymenttracker.feature.home.InsightsScreen
import codes.chirag.paymenttracker.feature.onboarding.OnboardingScreen
import codes.chirag.paymenttracker.feature.settings.SettingsScreen
import codes.chirag.paymenttracker.feature.settings.SettingsViewModel
import codes.chirag.paymenttracker.feature.transactions.SubscriptionViewModel
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
    subscriptionRepo: SubscriptionRepository,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsRepo = remember { PreferencesRepository(context) }
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

        homeGraph(innerPadding, navController, txRepo, profileRepo, prefsRepo)
        transactionsGraph(innerPadding, navController, txRepo, subscriptionRepo)
        goalsGraph(innerPadding, navController, goalRepo)
        settingsGraph(innerPadding, navController, profileRepo, txRepo, prefsRepo)
    }
}

/**
 * Home feature navigation graph
 */
private fun NavGraphBuilder.homeGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    txRepo: TransactionRepository,
    profileRepo: UserProfileRepository,
    prefsRepo: PreferencesRepository
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
                factory = HomeViewModel.factory(txRepo, profileRepo, prefsRepo)
            )
            HomeScreen(
                viewModel = homeViewModel,
                contentPadding = innerPadding,
                navController = navController
            )
        }
        composable<HomeRoute.Insights> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.HomeGraph)
            }
            val homeViewModel: HomeViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = HomeViewModel.factory(txRepo, profileRepo, prefsRepo)
            )
            InsightsScreen(
                viewModel = homeViewModel,
                onNavigateBack = { navController.navigateUp() },
                contentPadding = innerPadding
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
    txRepo: TransactionRepository,
    subscriptionRepo: SubscriptionRepository
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
            val subViewModel: SubscriptionViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = SubscriptionViewModel.factory(subscriptionRepo)
            )
            TransactionsScreen(
                viewModel = txViewModel,
                subscriptionViewModel = subViewModel,
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
                onNavigateBack = { navController.navigateUp() }
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
    txRepo: TransactionRepository,
    prefsRepo: PreferencesRepository
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
                factory = SettingsViewModel.factory(profileRepo, txRepo, context, prefsRepo)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onManageCategories = {
                    navController.navigate(SettingsRoute.ManageCategories)
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
        composable<SettingsRoute.ManageCategories> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Route.SettingsGraph)
            }
            val context = androidx.compose.ui.platform.LocalContext.current
            val settingsViewModel: SettingsViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = SettingsViewModel.factory(profileRepo, txRepo, context, prefsRepo)
            )
            codes.chirag.paymenttracker.feature.settings.ManageCategoriesScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.navigateUp() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}


