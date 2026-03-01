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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.BuildConfig
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    val userName = profile?.name?.ifBlank { "User" } ?: "User"
    val monthlyBudget = profile?.monthlyBudget ?: ""
    val budgetDisplay = if (monthlyBudget.isBlank()) "Not set" else "₹$monthlyBudget"
    val nameInitial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    // Toggle states — initialized from SharedPreferences via ViewModel
    var pushNotifications by remember { mutableStateOf(viewModel.getPushNotifications()) }
    var billReminders by remember { mutableStateOf(viewModel.getBillReminders()) }
    var budgetAlerts by remember { mutableStateOf(viewModel.getBudgetAlerts()) }
    var securityLock by remember { mutableStateOf(false) }

    // Dialog state
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Monthly Budget") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (budgetInput.isNotBlank()) {
                        viewModel.updateBudget(budgetInput.trim())
                    }
                    showBudgetDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text("Cancel") }
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

        // Profile card
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceL1)
                    .clickable { }
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
                            text = "Monthly budget: $budgetDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Preferences section
        item {
            SettingsSectionHeader("Preferences")
            SettingsSectionCard {
                SettingRowNavigation(
                    icon = Icons.Outlined.CurrencyRupee,
                    label = "Currency",
                    value = "INR (₹)",
                    onClick = {}
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon = Icons.Outlined.Category,
                    label = "Manage Categories",
                    onClick = {}
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon = Icons.Outlined.Receipt,
                    label = "Monthly Budget",
                    value = budgetDisplay,
                    onClick = {
                        budgetInput = monthlyBudget
                        showBudgetDialog = true
                    }
                )
                SettingsDivider()
                SettingRowToggle(
                    icon = Icons.Outlined.Fingerprint,
                    label = "Security Lock",
                    checked = securityLock,
                    onCheckedChange = { securityLock = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Notifications section
        item {
            SettingsSectionHeader("Notifications")
            SettingsSectionCard {
                SettingRowToggle(
                    icon = Icons.Outlined.Notifications,
                    label = "Push Notifications",
                    checked = pushNotifications,
                    onCheckedChange = {
                        pushNotifications = it
                        viewModel.setToggle("push_notifs", it)
                    }
                )
                SettingsDivider()
                SettingRowToggle(
                    icon = Icons.Outlined.Receipt,
                    label = "Bill Reminders",
                    checked = billReminders,
                    onCheckedChange = {
                        billReminders = it
                        viewModel.setToggle("bill_reminders", it)
                    }
                )
                SettingsDivider()
                SettingRowToggle(
                    icon = Icons.Outlined.Warning,
                    label = "Budget Alerts",
                    checked = budgetAlerts,
                    onCheckedChange = {
                        budgetAlerts = it
                        viewModel.setToggle("budget_alerts", it)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Data section
        item {
            SettingsSectionHeader("Data")
            SettingsSectionCard {
                SettingRowNavigation(
                    icon = Icons.Outlined.Upload,
                    label = "Export Data",
                    onClick = { viewModel.exportCsv(context) }
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon = Icons.Outlined.Download,
                    label = "Import Data",
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // About section
        item {
            SettingsSectionHeader("About")
            SettingsSectionCard {
                SettingRowNavigation(
                    icon = Icons.Outlined.Info,
                    label = "App Version",
                    value = BuildConfig.VERSION_NAME,
                    onClick = {}
                )
                SettingsDivider()
                SettingRowNavigation(
                    icon = Icons.Outlined.AccountCircle,
                    label = "Privacy Policy",
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceMuted,
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
        modifier = Modifier.padding(start = 56.dp),
        color = DividerColor,
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
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
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
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnPrimary,
                checkedTrackColor = OrangePrimary,
                uncheckedThumbColor = OnSurfaceMuted,
                uncheckedTrackColor = SurfaceL3,
                uncheckedBorderColor = BorderColor
            )
        )
    }
}
