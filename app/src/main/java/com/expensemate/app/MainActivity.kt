package com.expensemate.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.expensemate.app.domain.model.ThemeMode
import com.expensemate.app.ui.components.ResponsiveAppLayout
import com.expensemate.app.ui.navigation.AppNavGraph
import com.expensemate.app.ui.navigation.Screen
import com.expensemate.app.ui.theme.ExpenseMateTheme
import com.expensemate.app.util.rememberWindowSizeInfo

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission outcome recorded; app functions smoothly whether granted or not
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        val app = application as ExpenseMateApp
        val repository = app.repository

        setContent {
            val appSettings by repository.appSettingsFlow.collectAsState(
                initial = com.expensemate.app.domain.model.AppSettings()
            )

            ExpenseMateTheme(themeMode = appSettings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val windowInfo = rememberWindowSizeInfo()

                    // Top-level destinations where main navigation bar/rail should appear
                    val isTopLevelDestination = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.Transactions.route,
                        Screen.Budgets.route,
                        Screen.Reports.route,
                        Screen.Settings.route
                    )

                    if (isTopLevelDestination) {
                        ResponsiveAppLayout(
                            currentRoute = currentRoute,
                            windowType = windowInfo.widthType,
                            isLandscape = windowInfo.isLandscape,
                            onNavigate = { route ->
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onAddTransactionClick = {
                                navController.navigate(Screen.AddTransaction.createRoute("EXPENSE"))
                            }
                        ) {
                            AppNavGraph(
                                navController = navController,
                                repository = repository
                            )
                        }
                    } else {
                        // Full screen sub-destinations (e.g. Add/Edit, Categories, Accounts)
                        AppNavGraph(
                            navController = navController,
                            repository = repository
                        )
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
