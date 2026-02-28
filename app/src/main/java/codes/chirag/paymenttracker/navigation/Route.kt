package codes.chirag.paymenttracker.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app using type-safe navigation
 */

// Top-level graph used before the user completes onboarding
@Serializable
data object OnboardingGraph

// Root level routes for bottom navigation
sealed interface Route {
    @Serializable
    data object HomeGraph : Route

    @Serializable
    data object TransactionsGraph : Route

    @Serializable
    data object GoalsGraph : Route

    @Serializable
    data object SettingsGraph : Route
}

// Home feature routes
sealed interface HomeRoute {
    @Serializable
    data object Home : HomeRoute
}

// Transactions feature routes
sealed interface TransactionsRoute {
    @Serializable
    data object TransactionsList : TransactionsRoute

    @Serializable
    data class TransactionDetails(val transactionId: String) : TransactionsRoute

    @Serializable
    data class EditTransaction(val transactionId: String) : TransactionsRoute
}

// Goals feature routes
sealed interface GoalsRoute {
    @Serializable
    data object GoalsList : GoalsRoute

    @Serializable
    data class GoalDetail(val goalId: String) : GoalsRoute
}

// Settings feature routes
sealed interface SettingsRoute {
    @Serializable
    data object SettingsHome : SettingsRoute
}

// Onboarding feature routes
sealed interface OnboardingRoute {
    @Serializable
    data object Onboarding : OnboardingRoute
}

