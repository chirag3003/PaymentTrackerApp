package codes.chirag.paymenttracker.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.feature.home.HomeUiState
import codes.chirag.paymenttracker.ui.theme.ExpenseRed
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3

data class SpendingAlert(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val title: String,
    val subtitle: String
)

fun buildAlerts(state: HomeUiState): List<SpendingAlert> {
    val alerts = mutableListOf<SpendingAlert>()

    // 1. Daily budget threshold
    if (state.dailyBudget > 0) {
        val pct = (state.spentToday / state.dailyBudget * 100).toInt()
        when {
            state.spentToday >= state.dailyBudget -> alerts += SpendingAlert(
                icon = Icons.Outlined.Warning,
                iconTint = ExpenseRed,
                iconBg = ExpenseRed.copy(alpha = 0.15f),
                title = "Daily budget exceeded",
                subtitle = "You've spent ₹${state.spentToday.toLong()} today — ₹${state.dailyBudget.toLong()} limit"
            )
            pct >= 80 -> alerts += SpendingAlert(
                icon = Icons.Outlined.TrendingUp,
                iconTint = OrangePrimary,
                iconBg = OrangeSubtle,
                title = "Approaching daily limit",
                subtitle = "${pct}% of daily budget used (₹${state.spentToday.toLong()} / ₹${state.dailyBudget.toLong()})"
            )
        }
    }

    // 2. Category over 80%
    state.categorySpending.forEach { cat ->
        if (cat.budget > 0) {
            val pct = (cat.amount / cat.budget * 100).toInt()
            when {
                cat.isOverBudget -> alerts += SpendingAlert(
                    icon = Icons.Outlined.Warning,
                    iconTint = ExpenseRed,
                    iconBg = ExpenseRed.copy(alpha = 0.15f),
                    title = "${cat.category} over budget",
                    subtitle = "Spent ₹${cat.amount.toLong()} — budget ₹${cat.budget.toLong()}"
                )
                pct >= 80 -> alerts += SpendingAlert(
                    icon = Icons.Outlined.TrendingUp,
                    iconTint = OrangePrimary,
                    iconBg = OrangeSubtle,
                    title = "${cat.category} at ${pct}%",
                    subtitle = "₹${cat.amount.toLong()} of ₹${cat.budget.toLong()} used"
                )
            }
        }
    }

    // 3. Positive: monthly income received
    if (state.monthlyIncome > 0) {
        alerts += SpendingAlert(
            icon = Icons.Outlined.CheckCircle,
            iconTint = IncomeGreen,
            iconBg = IncomeGreen.copy(alpha = 0.12f),
            title = "Income recorded",
            subtitle = "₹${state.monthlyIncome.toLong()} received this month"
        )
    }

    if (alerts.isEmpty()) {
        alerts += SpendingAlert(
            icon = Icons.Outlined.Notifications,
            iconTint = OnSurfaceMuted,
            iconBg = SurfaceL3,
            title = "You're on track",
            subtitle = "No alerts right now. Keep it up!"
        )
    }

    return alerts
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingAlertsSheet(
    state: HomeUiState,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val alerts = buildAlerts(state)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceL1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Spending Alerts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Text(
                text = "${alerts.size} notification${if (alerts.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            alerts.forEach { alert ->
                AlertRow(alert)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AlertRow(alert: SpendingAlert) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceL3)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(alert.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = alert.icon,
                contentDescription = null,
                tint = alert.iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Text(
                text = alert.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted
            )
        }
    }
}
