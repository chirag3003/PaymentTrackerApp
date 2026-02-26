package codes.chirag.paymenttracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Settings Screen
 * Displays app settings organized into sections: Account, Preferences, Notifications, and Data
 *
 * @param modifier Optional modifier for styling
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Settings state
    var isDarkMode by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var billReminders by remember { mutableStateOf(true) }
    var budgetAlerts by remember { mutableStateOf(false) }
    var securityLock by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ACCOUNT Section
        SettingsSection(
            title = "ACCOUNT",
            items = listOf(
                SettingItem.Clickable(
                    icon = Icons.Default.AccountCircle,
                    title = "Profile Information",
                    onClick = { /* TODO: Navigate to profile */ }
                ),
                SettingItem.Clickable(
                    icon = Icons.Default.CreditCard,
                    title = "Subscription",
                    onClick = { /* TODO: Navigate to subscription */ }
                )
            )
        )

        // PREFERENCES Section
        SettingsSection(
            title = "PREFERENCES",
            items = listOf(
                SettingItem.Toggle(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    isChecked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                ),
                SettingItem.ClickableWithValue(
                    icon = Icons.Default.AttachMoney,
                    title = "Currency",
                    value = "USD",
                    onClick = { /* TODO: Show currency picker */ }
                ),
                SettingItem.Clickable(
                    icon = Icons.Default.Category,
                    title = "Manage Categories",
                    onClick = { /* TODO: Navigate to categories */ }
                ),
                SettingItem.Toggle(
                    icon = Icons.Default.Fingerprint,
                    title = "Security Lock",
                    isChecked = securityLock,
                    onCheckedChange = { securityLock = it }
                )
            )
        )

        // NOTIFICATIONS Section
        SettingsSection(
            title = "NOTIFICATIONS",
            items = listOf(
                SettingItem.Toggle(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    isChecked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                ),
                SettingItem.Toggle(
                    icon = Icons.Default.Receipt,
                    title = "Bill Reminders",
                    isChecked = billReminders,
                    onCheckedChange = { billReminders = it }
                ),
                SettingItem.Toggle(
                    icon = Icons.Default.Warning,
                    title = "Budget Alerts",
                    isChecked = budgetAlerts,
                    onCheckedChange = { budgetAlerts = it }
                )
            )
        )

        // DATA Section
        SettingsSection(
            title = "DATA",
            items = listOf(
                SettingItem.Clickable(
                    icon = Icons.Default.Upload,
                    title = "Export Data",
                    onClick = { /* TODO: Export data */ }
                ),
                SettingItem.Clickable(
                    icon = Icons.Default.Download,
                    title = "Import Data",
                    onClick = { /* TODO: Import data */ }
                )
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Log Out Button
        Button(
            onClick = { /* TODO: Handle logout */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF5350)
            )
        ) {
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Settings section with title and items
 */
@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Title
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        // Section Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items.forEachIndexed { index, item ->
                when (item) {
                    is SettingItem.Clickable -> {
                        ClickableSettingItem(
                            icon = item.icon,
                            title = item.title,
                            onClick = item.onClick
                        )
                    }
                    is SettingItem.ClickableWithValue -> {
                        ClickableSettingItemWithValue(
                            icon = item.icon,
                            title = item.title,
                            value = item.value,
                            onClick = item.onClick
                        )
                    }
                    is SettingItem.Toggle -> {
                        ToggleSettingItem(
                            icon = item.icon,
                            title = item.title,
                            isChecked = item.isChecked,
                            onCheckedChange = item.onCheckedChange
                        )
                    }
                }

                // Divider between items (except last item)
                if (index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(start = 72.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                }
            }
        }
    }
}

/**
 * Clickable setting item with icon and title
 */
@Composable
private fun ClickableSettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Clickable setting item with icon, title, and value
 */
@Composable
private fun ClickableSettingItemWithValue(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Value
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Toggle setting item with icon, title, and switch
 */
@Composable
private fun ToggleSettingItem(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Switch
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Sealed class representing different types of setting items
 */
private sealed class SettingItem {
    data class Clickable(
        val icon: ImageVector,
        val title: String,
        val onClick: () -> Unit
    ) : SettingItem()

    data class ClickableWithValue(
        val icon: ImageVector,
        val title: String,
        val value: String,
        val onClick: () -> Unit
    ) : SettingItem()

    data class Toggle(
        val icon: ImageVector,
        val title: String,
        val isChecked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingItem()
}

