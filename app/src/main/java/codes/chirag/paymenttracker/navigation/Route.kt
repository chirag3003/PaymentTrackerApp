package codes.chirag.paymenttracker.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app using type-safe navigation
 */

// Root level routes for bottom navigation
sealed interface Route {
    @Serializable
    data object HomeGraph : Route

    @Serializable
    data object TransactionsGraph : Route

    @Serializable
    data object AnalysisGraph : Route

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
}

// Analysis feature routes
sealed interface AnalysisRoute {
    @Serializable
    data object AnalysisHome : AnalysisRoute
}

// Settings feature routes
sealed interface SettingsRoute {
    @Serializable
    data object SettingsHome : SettingsRoute
}

