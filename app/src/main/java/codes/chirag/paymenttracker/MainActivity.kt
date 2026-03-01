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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import codes.chirag.paymenttracker.core.data.repository.GoalRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.database.AppDatabase
import codes.chirag.paymenttracker.core.database.DatabaseSeeder
import codes.chirag.paymenttracker.navigation.BottomNavDestination
import codes.chirag.paymenttracker.navigation.OnboardingRoute
import codes.chirag.paymenttracker.navigation.PaymentTrackerNavHost
import codes.chirag.paymenttracker.ui.theme.PaymentTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PREFS_NAME = "payment_tracker_prefs"
const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
const val KEY_USER_NAME = "user_name"
const val KEY_MONTHLY_BUDGET = "monthly_budget"
const val KEY_PREFERRED_METHOD = "preferred_method"

class MainActivity : ComponentActivity() {

    private lateinit var txRepo: TransactionRepository
    private lateinit var goalRepo: GoalRepository
    internal lateinit var profileRepo: UserProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(this)
        txRepo      = TransactionRepository(db.transactionDao())
        goalRepo    = GoalRepository(db.goalDao())
        profileRepo = UserProfileRepository(db.userProfileDao())

        lifecycleScope.launch(Dispatchers.IO) {
            DatabaseSeeder.seedIfEmpty(txRepo, goalRepo)
        }

        enableEdgeToEdge()
        setContent {
            PaymentTrackerTheme {
                PaymentTrackerApp(
                    txRepo      = txRepo,
                    goalRepo    = goalRepo,
                    profileRepo = profileRepo
                )
            }
        }
    }
}

@Composable
fun PaymentTrackerApp(
    txRepo: TransactionRepository,
    goalRepo: GoalRepository,
    profileRepo: UserProfileRepository
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

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
                                    popUpTo(navController.graph.findStartDestination().id) {
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
            navController   = navController,
            innerPadding    = innerPadding,
            showOnboarding  = showOnboarding,
            txRepo          = txRepo,
            goalRepo        = goalRepo,
            profileRepo     = profileRepo,
            onOnboardingComplete = { name, budget, method ->
                prefs.edit()
                    .putBoolean(KEY_ONBOARDING_COMPLETE, true)
                    .putString(KEY_USER_NAME, name)
                    .putString(KEY_MONTHLY_BUDGET, budget)
                    .putString(KEY_PREFERRED_METHOD, method.name)
                    .apply()
                // Keep Room in sync with the onboarding values
                coroutineScope.launch(Dispatchers.IO) {
                    profileRepo.save(name, budget, method.name)
                }
                showOnboarding = false
            }
        )
    }
}
