package codes.chirag.paymenttracker

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import codes.chirag.paymenttracker.navigation.BottomNavDestination
import codes.chirag.paymenttracker.navigation.PaymentTrackerNavHost
import codes.chirag.paymenttracker.ui.theme.PaymentTrackerTheme
import kotlin.reflect.typeOf

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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(), bottomBar = {
            NavigationBar {
                BottomNavDestination.entries.forEach { destination ->
//                    val isSelected = currentDestination?.hierarchy?.any {
//                        it.hasRoute(destination.title, typeOf<Any>())
//                    } == true
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.hasRoute(destination.route::class)
                    } == true
                    NavigationBarItem(selected = isSelected, onClick = {
                        navController.navigate(destination.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }, icon = {
                        Icon(
                            imageVector = if (isSelected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            }, contentDescription = destination.title
                        )
                    }, label = { Text(destination.title) })
                }
            }
        }) { innerPadding ->
        PaymentTrackerNavHost(
            navController = navController, innerPadding = innerPadding
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentTrackerAppPreview() {
    PaymentTrackerTheme {
        PaymentTrackerApp()
    }
}