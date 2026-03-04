package codes.chirag.paymenttracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.BuildConfig
import codes.chirag.paymenttracker.core.biometric.BiometricLockManager
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onManageCategories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    val userName      = profile?.name?.ifBlank { "User" } ?: "User"
    val monthlyBudget = profile?.monthlyBudget ?: ""
    val budgetDisplay = if (monthlyBudget.isBlank()) "Not set" else "₹$monthlyBudget"
    val preferredMethod = profile?.preferredMethod
        ?.let { runCatching { PaymentMethod.valueOf(it) }.getOrNull() }
        ?: PaymentMethod.UPI
    val nameInitial   = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    // AI Configuration state
    var activeAiModel     by remember { mutableStateOf(viewModel.getActiveAiModel()) }
    var geminiApiKey      by remember { mutableStateOf(viewModel.getGeminiApiKey()) }
    var anthropicApiKey   by remember { mutableStateOf(viewModel.getAnthropicApiKey()) }
    var showGeminiKey     by remember { mutableStateOf(false) }
    var showAnthropicKey  by remember { mutableStateOf(false) }

    // Toggle states — initialized from SharedPreferences via ViewModel
    var pushNotifications by remember { mutableStateOf(viewModel.getPushNotifications()) }
    var billReminders     by remember { mutableStateOf(viewModel.getBillReminders()) }
    var budgetAlerts      by remember { mutableStateOf(viewModel.getBudgetAlerts()) }
    var securityLock      by remember { mutableStateOf(viewModel.getBiometricLock()) }

    // Sheet / dialog visibility
    var showEditProfileSheet  by rememberSaveable { mutableStateOf(false) }
    var showCurrencyDialog    by rememberSaveable { mutableStateOf(false) }
    var showImportDialog      by rememberSaveable { mutableStateOf(false) }
    var showNoBiometricDialog by rememberSaveable { mutableStateOf(false) }

    val editProfileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // ── Edit Profile Sheet ────────────────────────────────────────────────────
    if (showEditProfileSheet) {
        EditProfileBottomSheet(
            initialName   = userName.takeIf { it != "User" } ?: "",
            initialBudget = monthlyBudget,
            initialMethod = preferredMethod,
            sheetState    = editProfileSheetState,
            onDismiss = {
                scope.launch { editProfileSheetState.hide() }.invokeOnCompletion {
                    showEditProfileSheet = false
                }
            },
            onSave = { name, budget, method ->
                viewModel.updateProfile(name, budget, method)
                scope.launch { editProfileSheetState.hide() }.invokeOnCompletion {
                    showEditProfileSheet = false
                }
            }
        )
    }

    // ── Currency dialog ───────────────────────────────────────────────────────
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Currency") },
            text  = { Text("This app uses Indian Rupee (₹ INR) as its only currency.") },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Got it") }
            }
        )
    }

    // ── Import coming soon dialog ─────────────────────────────────────────────
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data") },
            text  = { Text("CSV import is coming soon. Stay tuned for updates!") },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("OK") }
            }
        )
    }

    // ── No biometric enrolled dialog ──────────────────────────────────────────
    if (showNoBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showNoBiometricDialog = false },
            title = { Text("Biometric Unavailable") },
            text  = { Text("No biometric or screen lock is set up on this device. Go to Settings → Security to enrol a fingerprint, face, or PIN first.") },
            confirmButton = {
                TextButton(onClick = { showNoBiometricDialog = false }) { Text("OK") }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = OnBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        // ── Profile card ──────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceL1)
                    .clickable { showEditProfileSheet = true }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(OrangeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nameInitial,
                            style = MaterialTheme.typography.headlineSmall,
                            color = OrangePrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = OnBackground
                        )
                        Text(
                            text = "Budget: $budgetDisplay  ·  ${preferredMethod.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit profile",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Preferences ───────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("Preferences")
            SettingsSectionCard {
                SettingRowNavigation(
                    icon    = Icons.Outlined.CurrencyRupee,
                    label   = "Currency",
                    value   = "INR (₹)",
                    onClick = { showCurrencyDialog = true }
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon    = Icons.Outlined.Category,
                    label   = "Manage Categories",
                    onClick = onManageCategories
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon    = Icons.Outlined.Receipt,
                    label   = "Monthly Budget",
                    value   = budgetDisplay,
                    onClick = { showEditProfileSheet = true }
                )
                SettingsDivider()
                SettingRowToggle(
                    icon           = Icons.Outlined.Fingerprint,
                    label          = "Biometric Lock",
                    checked        = securityLock,
                    onCheckedChange = { requested ->
                        if (requested) {
                            if (!BiometricLockManager.isAvailable(context)) {
                                showNoBiometricDialog = true
                                return@SettingRowToggle
                            }
                            // Verify biometrics before enabling
                            BiometricLockManager.authenticate(
                                context   = context,
                                title     = "Enable Biometric Lock",
                                subtitle  = "Confirm your identity to enable the lock",
                                onSuccess = {
                                    securityLock = true
                                    viewModel.setBiometricLock(true)
                                },
                                onFailure = { /* switch stays OFF */ }
                            )
                        } else {
                            securityLock = false
                            viewModel.setBiometricLock(false)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── AI Configuration ──────────────────────────────────────────────────
        item {
            SettingsSectionHeader("AI Configuration")
            SettingsSectionCard {
                // Active Model Selector
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Active AI Model",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceMuted
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceL3)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val models = listOf("GEMINI" to "Gemini Flash", "CLAUDE" to "Claude Haiku")
                        models.forEach { (modelId, label) ->
                            val isSelected = activeAiModel == modelId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) OrangePrimary else SurfaceL3)
                                    .clickable {
                                        activeAiModel = modelId
                                        viewModel.setActiveAiModel(modelId)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) OnPrimary else OnSurfaceMuted
                                )
                            }
                        }
                    }
                }
                
                SettingsDivider()
                
                // Gemini API Key
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Gemini API Key",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceMuted
                    )
                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { 
                            geminiApiKey = it
                            viewModel.setGeminiApiKey(it.trim())
                        },
                        placeholder = { Text("AIzaSy...", color = OnSurfaceMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showGeminiKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                Icon(
                                    imageVector = if (showGeminiKey) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = OnSurfaceMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnBackground,
                            unfocusedTextColor = OnBackground,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = SurfaceL3,
                            unfocusedContainerColor = SurfaceL3
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                SettingsDivider()

                // Anthropic API Key
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Anthropic API Key",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceMuted
                    )
                    OutlinedTextField(
                        value = anthropicApiKey,
                        onValueChange = { 
                            anthropicApiKey = it
                            viewModel.setAnthropicApiKey(it.trim())
                        },
                        placeholder = { Text("sk-ant-...", color = OnSurfaceMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showAnthropicKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { showAnthropicKey = !showAnthropicKey }) {
                                Icon(
                                    imageVector = if (showAnthropicKey) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = OnSurfaceMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnBackground,
                            unfocusedTextColor = OnBackground,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = SurfaceL3,
                            unfocusedContainerColor = SurfaceL3
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Notifications ─────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("Notifications")
            SettingsSectionCard {
                SettingRowToggle(
                    icon           = Icons.Outlined.Notifications,
                    label          = "Push Notifications",
                    checked        = pushNotifications,
                    onCheckedChange = {
                        pushNotifications = it
                        viewModel.setToggle("push_notifs", it)
                    }
                )
                SettingsDivider()
                SettingRowToggle(
                    icon           = Icons.Outlined.Receipt,
                    label          = "Bill Reminders",
                    checked        = billReminders,
                    onCheckedChange = {
                        billReminders = it
                        viewModel.setToggle("bill_reminders", it)
                    }
                )
                SettingsDivider()
                SettingRowToggle(
                    icon           = Icons.Outlined.Warning,
                    label          = "Budget Alerts",
                    checked        = budgetAlerts,
                    onCheckedChange = {
                        budgetAlerts = it
                        viewModel.setToggle("budget_alerts", it)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Data ──────────────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("Data")
            SettingsSectionCard {
                SettingRowNavigation(
                    icon    = Icons.Outlined.Upload,
                    label   = "Export Data",
                    onClick = { viewModel.exportCsv(context) }
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon    = Icons.Outlined.Download,
                    label   = "Import Data",
                    onClick = { showImportDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── About ─────────────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("About")
            SettingsSectionCard {
                SettingRowNavigation(
                    icon    = Icons.Outlined.Info,
                    label   = "App Version",
                    value   = BuildConfig.VERSION_NAME,
                    onClick = {}
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon    = Icons.Outlined.AccountCircle,
                    label   = "Privacy Policy",
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Edit Profile Bottom Sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    initialName: String,
    initialBudget: String,
    initialMethod: PaymentMethod,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onSave: (name: String, budget: String, method: PaymentMethod) -> Unit
) {
    var name   by rememberSaveable { mutableStateOf(initialName) }
    var budget by rememberSaveable { mutableStateOf(initialBudget) }
    var method by rememberSaveable { mutableStateOf(initialMethod) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceL1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceL3)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Name field
            ProfileFieldLabel("Your Name") {
                OutlinedTextField(
                    value       = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Chirag", color = OnSurfaceMuted) },
                    singleLine  = true,
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(12.dp),
                    colors      = profileTextFieldColors()
                )
            }

            // Budget field
            ProfileFieldLabel("Monthly Budget (₹)") {
                OutlinedTextField(
                    value       = budget,
                    onValueChange = { budget = it },
                    placeholder = { Text("e.g. 15000", color = OnSurfaceMuted) },
                    singleLine  = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(12.dp),
                    colors      = profileTextFieldColors()
                )
            }

            // Payment method chips
            ProfileFieldLabel("Preferred Payment Method") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaymentMethod.entries) { m ->
                        val selected = m == method
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) OrangePrimary else SurfaceL3)
                                .clickable { method = m }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text  = m.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) OnPrimary else OnSurfaceMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save button
            Button(
                onClick = {
                    onSave(name.trim(), budget.trim(), method)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor   = OnPrimary
                )
            ) {
                Text("Save Changes", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun ProfileFieldLabel(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)
        content()
    }
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor      = OnBackground,
    unfocusedTextColor    = OnBackground,
    focusedBorderColor    = OrangePrimary,
    unfocusedBorderColor  = BorderColor,
    cursorColor           = OrangePrimary,
    focusedContainerColor = SurfaceL3,
    unfocusedContainerColor = SurfaceL3
)

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelMedium,
        color    = OnSurfaceMuted,
        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSectionCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceL1)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 56.dp),
        color     = DividerColor,
        thickness = 0.5.dp
    )
}

@Composable
private fun SettingRowNavigation(
    icon: ImageVector,
    label: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SurfaceL3),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = OnBackground,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text  = value,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceMuted
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingRowToggle(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SurfaceL3),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = OnBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = OnPrimary,
                checkedTrackColor   = OrangePrimary,
                uncheckedThumbColor = OnSurfaceMuted,
                uncheckedTrackColor = SurfaceL3,
                uncheckedBorderColor = BorderColor
            )
        )
    }
}
