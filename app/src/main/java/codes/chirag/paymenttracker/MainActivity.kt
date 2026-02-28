package codes.chirag.paymenttracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import codes.chirag.paymenttracker.navigation.BottomNavDestination
import codes.chirag.paymenttracker.navigation.OnboardingRoute
import codes.chirag.paymenttracker.navigation.PaymentTrackerNavHost
import codes.chirag.paymenttracker.ui.theme.PaymentTrackerTheme

private const val PREFS_NAME = "payment_tracker_prefs"
private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaymentTrackerTheme {
                PaymentTrackerApp()
            }
        }
    }
}

@Composable
fun PaymentTrackerApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var showOnboarding by remember {
        mutableStateOf(!prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false))
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isOnboardingVisible = currentDestination?.hasRoute(OnboardingRoute.Onboarding::class) == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isOnboardingVisible) {
                NavigationBar {
                    BottomNavDestination.entries.forEach { destination ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(destination.route::class)
                        } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon
                                                  else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PaymentTrackerNavHost(
            navController = navController,
            innerPadding = innerPadding,
            showOnboarding = showOnboarding,
            onOnboardingComplete = {
                prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
                showOnboarding = false
            }
        )
    }
}
