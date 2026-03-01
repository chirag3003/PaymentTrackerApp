package codes.chirag.paymenttracker

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import codes.chirag.paymenttracker.core.biometric.BiometricLockManager
import codes.chirag.paymenttracker.core.data.repository.GoalRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.database.AppDatabase
import codes.chirag.paymenttracker.core.database.DatabaseSeeder
import codes.chirag.paymenttracker.navigation.BottomNavDestination
import codes.chirag.paymenttracker.navigation.OnboardingRoute
import codes.chirag.paymenttracker.navigation.PaymentTrackerNavHost
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.PaymentTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PREFS_NAME = "payment_tracker_prefs"
const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
const val KEY_USER_NAME = "user_name"
const val KEY_MONTHLY_BUDGET = "monthly_budget"
const val KEY_PREFERRED_METHOD = "preferred_method"

class MainActivity : FragmentActivity() {

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
    val prefs   = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var showOnboarding by remember {
        mutableStateOf(!prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false))
    }

    // ── Biometric lock state ──────────────────────────────────────────────────
    // isLocked starts true if lock is enabled AND needs_unlock is persisted.
    // This covers both cold starts and resume-from-background.
    val lockEnabled = prefs.getBoolean("biometric_lock", false)
    var isLocked by remember {
        mutableStateOf(lockEnabled && prefs.getBoolean("needs_unlock", false))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Persist the "needs unlock on next resume" flag
                    if (prefs.getBoolean("biometric_lock", false) && !showOnboarding) {
                        prefs.edit().putBoolean("needs_unlock", true).apply()
                        isLocked = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Re-check in case biometric was toggled while app was running
                    if (prefs.getBoolean("biometric_lock", false) &&
                        prefs.getBoolean("needs_unlock", false)
                    ) {
                        isLocked = true
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Lock screen overlay ───────────────────────────────────────────────────
    if (isLocked) {
        LockScreen(
            onUnlock = {
                BiometricLockManager.authenticate(
                    context   = context,
                    title     = "Unlock Payment Tracker",
                    subtitle  = "Verify your identity to continue",
                    onSuccess = {
                        prefs.edit().putBoolean("needs_unlock", false).apply()
                        isLocked = false
                    },
                    onFailure = { /* stay locked — user can retry via button */ }
                )
            }
        )
        return
    }

    // ── Normal app UI ─────────────────────────────────────────────────────────
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isOnboardingVisible =
        currentDestination?.hasRoute(OnboardingRoute.Onboarding::class) == true

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
                            onClick  = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon = {
                                androidx.compose.material3.Icon(
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
            navController        = navController,
            innerPadding         = innerPadding,
            showOnboarding       = showOnboarding,
            txRepo               = txRepo,
            goalRepo             = goalRepo,
            profileRepo          = profileRepo,
            onOnboardingComplete = { name, budget, method ->
                prefs.edit()
                    .putBoolean(KEY_ONBOARDING_COMPLETE, true)
                    .putString(KEY_USER_NAME, name)
                    .putString(KEY_MONTHLY_BUDGET, budget)
                    .putString(KEY_PREFERRED_METHOD, method.name)
                    .apply()
                coroutineScope.launch(Dispatchers.IO) {
                    profileRepo.save(name, budget, method.name)
                }
                showOnboarding = false
            }
        )
    }
}

// ── Lock Screen ───────────────────────────────────────────────────────────────

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(OrangeSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text  = "Payment Tracker",
                style = MaterialTheme.typography.headlineSmall,
                color = OnBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text      = "Verify your identity to continue",
                style     = MaterialTheme.typography.bodyMedium,
                color     = OnSurfaceMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onUnlock,
                shape   = RoundedCornerShape(14.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor   = OnPrimary
                ),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Unlock", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
