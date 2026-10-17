package com.expensemate.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.expensemate.app.ui.navigation.Screen
import com.expensemate.app.util.WindowType

data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val navigationItems = listOf(
    NavItem(Screen.Dashboard.route, "Home", Icons.Default.Dashboard),
    NavItem(Screen.Transactions.route, "Transactions", Icons.Default.ReceiptLong),
    NavItem(Screen.Budgets.route, "Budgets", Icons.Default.PieChart),
    NavItem(Screen.Reports.route, "Reports", Icons.Default.BarChart),
    NavItem(Screen.Settings.route, "Settings", Icons.Default.Settings)
)

@Composable
fun ResponsiveAppLayout(
    currentRoute: String?,
    windowType: WindowType,
    isLandscape: Boolean,
    onNavigate: (String) -> Unit,
    onAddTransactionClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val useNavRail = windowType != WindowType.COMPACT || isLandscape

    if (useNavRail) {
        // Landscape or Tablet Layout: NavigationRail on the left, Content on the right
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    FloatingActionButton(
                        onClick = onAddTransactionClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                for (item in navigationItems) {
                    val selected = currentRoute == item.route
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }

            // Main content area
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    } else {
        // Compact Phone Layout: Bottom NavigationBar and Scaffold
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    for (item in navigationItems) {
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTransactionClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                content()
            }
        }
    }
}
