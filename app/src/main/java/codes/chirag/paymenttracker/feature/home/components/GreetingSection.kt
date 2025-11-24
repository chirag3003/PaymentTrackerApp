package codes.chirag.paymenttracker.feature.home.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun GreetingSection(
    userName: String = "Chirag"
) {
    Text(
        text = "Welcome back, $userName!",
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

