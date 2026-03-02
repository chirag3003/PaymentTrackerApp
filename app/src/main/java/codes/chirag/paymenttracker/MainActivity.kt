package codes.chirag.paymenttracker

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import codes.chirag.paymenttracker.core.biometric.BiometricLockManager
import codes.chirag.paymenttracker.core.data.repository.GoalRepository
import codes.chirag.paymenttracker.core.data.repository.SubscriptionRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.database.AppDatabase
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Subscription
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import java.util.UUID
import codes.chirag.paymenttracker.feature.transactions.TransactionViewModel
import codes.chirag.paymenttracker.feature.transactions.components.AddTransactionBottomSheet
import codes.chirag.paymenttracker.feature.transactions.components.QuickAddBottomSheet
import codes.chirag.paymenttracker.feature.transactions.components.RecurringInfo
import codes.chirag.paymenttracker.navigation.BottomNavDestination
import codes.chirag.paymenttracker.navigation.OnboardingRoute
import codes.chirag.paymenttracker.navigation.PaymentTrackerNavHost
import codes.chirag.paymenttracker.navigation.Route
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.PaymentTrackerTheme
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
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
    private lateinit var subscriptionRepo: SubscriptionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(this)
        txRepo           = TransactionRepository(db.transactionDao())
        goalRepo         = GoalRepository(db.goalDao())
        profileRepo      = UserProfileRepository(db.userProfileDao())
        subscriptionRepo = SubscriptionRepository(db.subscriptionDao())

        enableEdgeToEdge()
        setContent {
            PaymentTrackerTheme {
                PaymentTrackerApp(
                    txRepo           = txRepo,
                    goalRepo         = goalRepo,
                    profileRepo      = profileRepo,
                    subscriptionRepo = subscriptionRepo
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentTrackerApp(
    txRepo: TransactionRepository,
    goalRepo: GoalRepository,
    profileRepo: UserProfileRepository,
    subscriptionRepo: SubscriptionRepository
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var showOnboarding by remember {
        mutableStateOf(!prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false))
    }

    // ── Biometric lock state ──────────────────────────────────────────────────
    val lockEnabled = prefs.getBoolean("biometric_lock", false)
    var isLocked by remember {
        mutableStateOf(lockEnabled && prefs.getBoolean("needs_unlock", false))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (prefs.getBoolean("biometric_lock", false) && !showOnboarding) {
                        prefs.edit().putBoolean("needs_unlock", true).apply()
                        isLocked = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
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
                    onFailure = { /* stay locked */ }
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

    // Determine active top-level graph
    val isGoalsTab    = currentDestination?.hierarchy?.any { it.hasRoute(Route.GoalsGraph::class) }    == true
    val isSettingsTab = currentDestination?.hierarchy?.any { it.hasRoute(Route.SettingsGraph::class) } == true
    val showGlobalFab = !isOnboardingVisible && !isGoalsTab && !isSettingsTab

    // ── Global FAB state ──────────────────────────────────────────────────────
    var fabExpanded by rememberSaveable { mutableStateOf(false) }

    // Sheet visibility
    var showAddSheet   by rememberSaveable { mutableStateOf(false) }
    var showQuickSheet by rememberSaveable { mutableStateOf(false) }

    // Pre-fill state for AddTransactionBottomSheet (populated by QuickAdd)
    var prefillTitle    by rememberSaveable { mutableStateOf("") }
    var prefillAmount   by rememberSaveable { mutableStateOf("") }
    var prefillType     by rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    var prefillCategory by rememberSaveable { mutableStateOf("Food") }
    var prefillMethod   by rememberSaveable { mutableStateOf(PaymentMethod.UPI) }
    var prefillNotes    by rememberSaveable { mutableStateOf("") }
    // Key to force re-composition of AddSheet with fresh prefill values
    var prefillKey      by rememberSaveable { mutableStateOf(0) }

    val addSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quickSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Resolve TransactionViewModel scoped to TransactionsGraph ─────────────
    // Only available when we're inside the transactions back-stack
    val txGraphEntry = remember(navBackStackEntry) {
        runCatching { navController.getBackStackEntry(Route.TransactionsGraph) }.getOrNull()
    }
    val txViewModel: TransactionViewModel? = if (txGraphEntry != null) {
        viewModel(
            viewModelStoreOwner = txGraphEntry,
            factory = TransactionViewModel.factory(txRepo)
        )
    } else null

    // Camera launcher (Scan Bill — capture result but no processing yet)
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { /* bitmap captured; AI processing in Phase D */ }

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
        },
        floatingActionButton = {
            if (showGlobalFab) {
                SpeedDialFab(
                    expanded = fabExpanded,
                    onToggle = { fabExpanded = !fabExpanded },
                    onManual = {
                        fabExpanded = false
                        prefillTitle = ""; prefillAmount = ""; prefillType = TransactionType.EXPENSE
                        prefillCategory = "Food"; prefillMethod = PaymentMethod.UPI; prefillNotes = ""
                        prefillKey++
                        showAddSheet = true
                    },
                    onQuickAdd = {
                        fabExpanded = false
                        showQuickSheet = true
                    },
                    onScanBill = {
                        fabExpanded = false
                        cameraLauncher.launch(null)
                    }
                )
            }
        }
    ) { innerPadding ->
        // Scrim overlay when FAB is expanded
        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { fabExpanded = false }
            )
        }

        PaymentTrackerNavHost(
            navController        = navController,
            innerPadding         = innerPadding,
            showOnboarding       = showOnboarding,
            txRepo               = txRepo,
            goalRepo             = goalRepo,
            profileRepo          = profileRepo,
            subscriptionRepo     = subscriptionRepo,
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

    // ── QuickAdd sheet ────────────────────────────────────────────────────────
    if (showQuickSheet) {
        QuickAddBottomSheet(
            sheetState = quickSheetState,
            onDismiss = {
                coroutineScope.launch { quickSheetState.hide() }.invokeOnCompletion {
                    showQuickSheet = false
                }
            },
            onParsed = { title, amount, type, category, method, notes ->
                coroutineScope.launch { quickSheetState.hide() }.invokeOnCompletion {
                    showQuickSheet = false
                    prefillTitle    = title
                    prefillAmount   = amount
                    prefillType     = type
                    prefillCategory = category
                    prefillMethod   = method
                    prefillNotes    = notes
                    prefillKey++
                    showAddSheet = true
                }
            }
        )
    }

    // ── Add Transaction sheet ─────────────────────────────────────────────────
    if (showAddSheet) {
        key(prefillKey) {
            AddTransactionBottomSheet(
                sheetState     = addSheetState,
                onDismiss = {
                    coroutineScope.launch { addSheetState.hide() }.invokeOnCompletion {
                        showAddSheet = false
                    }
                },
                onSave = { title, amountStr, type, category, paymentMethod, notes, date, recurring ->
                    // Save transaction via scoped ViewModel if available, otherwise direct repo call
                    if (txViewModel != null) {
                        txViewModel.add(title, amountStr, type, category, paymentMethod, notes, date)
                    } else {
                        val amount = amountStr.toDoubleOrNull()
                        if (amount != null) {
                            coroutineScope.launch(Dispatchers.IO) {
                                txRepo.add(
                                    Transaction(
                                        id            = UUID.randomUUID().toString(),
                                        title         = title,
                                        amount        = amount,
                                        type          = type,
                                        category      = category,
                                        date          = date,
                                        paymentMethod = paymentMethod,
                                        notes         = notes
                                    )
                                )
                            }
                        }
                    }
                    // If recurring, also create a subscription entry
                    if (recurring != null) {
                        val amount = amountStr.toDoubleOrNull()
                        if (amount != null) {
                            coroutineScope.launch(Dispatchers.IO) {
                                subscriptionRepo.add(
                                    Subscription(
                                        id            = UUID.randomUUID().toString(),
                                        name          = title,
                                        amount        = amount,
                                        frequency     = recurring.frequency,
                                        nextDueDate   = recurring.nextDueDate,
                                        category      = category,
                                        paymentMethod = paymentMethod,
                                        isActive      = true
                                    )
                                )
                            }
                        }
                    }
                    coroutineScope.launch { addSheetState.hide() }.invokeOnCompletion {
                        showAddSheet = false
                    }
                },
                initialTitle    = prefillTitle,
                initialAmount   = prefillAmount,
                initialType     = prefillType,
                initialCategory = prefillCategory,
                initialMethod   = prefillMethod,
                initialNotes    = prefillNotes
            )
        }
    }
}

// ── Speed-Dial FAB ────────────────────────────────────────────────────────────

@Composable
private fun SpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onManual: () -> Unit,
    onQuickAdd: () -> Unit,
    onScanBill: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(200),
        label = "fabRotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        // Mini FABs — visible when expanded
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(150)) + slideInVertically(tween(200)) { it / 2 } + scaleIn(tween(150)),
            exit  = fadeOut(tween(100)) + slideOutVertically(tween(150)) { it / 2 } + scaleOut(tween(100))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniFabRow(
                    icon = Icons.Outlined.CameraAlt,
                    label = "Scan Bill",
                    onClick = onScanBill
                )
                MiniFabRow(
                    icon = Icons.Outlined.Bolt,
                    label = "Quick Add",
                    onClick = onQuickAdd
                )
                MiniFabRow(
                    icon = Icons.Outlined.Edit,
                    label = "Manual",
                    onClick = onManual
                )
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            containerColor = OrangePrimary,
            contentColor = OnPrimary,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.Close else Icons.Filled.Add,
                contentDescription = if (expanded) "Close" else "Add",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun MiniFabRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Label pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceL1)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = OnBackground
            )
        }
        // Mini FAB
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = SurfaceL1,
            contentColor = OrangePrimary,
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
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
