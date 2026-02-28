package codes.chirag.paymenttracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom navigation destinations
 */
enum class BottomNavDestination(
    val route: Route,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = Route.HomeGraph,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    TRANSACTIONS(
        route = Route.TransactionsGraph,
        title = "Transactions",
        selectedIcon = Icons.Filled.SwapHoriz,
        unselectedIcon = Icons.Outlined.SwapHoriz
    ),
    GOALS(
        route = Route.GoalsGraph,
        title = "Goals",
        selectedIcon = Icons.Filled.Savings,
        unselectedIcon = Icons.Outlined.Savings
    ),
    SETTINGS(
        route = Route.SettingsGraph,
        title = "Settings",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )
}

