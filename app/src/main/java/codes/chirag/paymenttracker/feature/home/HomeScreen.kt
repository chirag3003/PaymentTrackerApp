package codes.chirag.paymenttracker.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.feature.home.components.GreetingSection
import codes.chirag.paymenttracker.feature.home.components.TotalBalanceCard

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp)
    ) {
        GreetingSection()
        TotalBalanceCard(
            totalBalance = 1234.56,
            income = 2500.00,
            expenses = 1265.44
        )
    }
}

