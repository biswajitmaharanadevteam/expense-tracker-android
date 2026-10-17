package com.expensemate.app.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.DateFormatOption
import com.expensemate.app.domain.model.FirstDayOfWeek
import com.expensemate.app.domain.model.ThemeMode
import com.expensemate.app.ui.components.ConfirmationDialog
import com.expensemate.app.ui.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToCategories: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showFirstDayDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // File pickers for Import CSV & JSON Restore
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                viewModel.importCsv(stream)
            }
        }
    }

    val jsonPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                viewModel.restoreBackupJson(stream)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.shareEvent.collect { uri ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (uri.toString().endsWith(".json")) "application/json" else "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Export File"))
        }
    }

    // Reset Data Confirmation Dialog
    ConfirmationDialog(
        show = uiState.showResetConfirmation,
        title = "Reset All Application Data?",
        message = "This will permanently erase all transactions, custom categories, accounts, and budgets from this device. This cannot be undone.",
        confirmButtonText = "Erase Everything",
        isDestructive = true,
        onConfirm = { viewModel.confirmResetData() },
        onDismiss = { viewModel.dismissResetDialog() }
    )

    // Status / Import Result Alert
    if (uiState.statusMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissStatusMessage() },
            title = { Text("Information") },
            text = {
                Column {
                    Text(uiState.statusMessage ?: "")
                    if (uiState.importResult?.errors?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Warnings:\n" + uiState.importResult!!.errors.take(3).joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissStatusMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    // Currency Selector Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    for (curr in CurrencyConfig.SUPPORTED_CURRENCIES) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(curr)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.appSettings.currency.code == curr.code,
                                onClick = {
                                    viewModel.setCurrency(curr)
                                    showCurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${curr.symbol}  -  ${curr.name}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Theme Selector Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    for (theme in ThemeMode.values()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.appSettings.themeMode == theme,
                                onClick = {
                                    viewModel.setThemeMode(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = theme.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Format Dialog
    if (showDateFormatDialog) {
        AlertDialog(
            onDismissRequest = { showDateFormatDialog = false },
            title = { Text("Date Format") },
            text = {
                Column {
                    for (df in DateFormatOption.values()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDateFormat(df)
                                    showDateFormatDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.appSettings.dateFormat == df,
                                onClick = {
                                    viewModel.setDateFormat(df)
                                    showDateFormatDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${df.pattern}  (${df.preview})")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDateFormatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // First Day of Week Dialog
    if (showFirstDayDialog) {
        AlertDialog(
            onDismissRequest = { showFirstDayDialog = false },
            title = { Text("First Day of the Week") },
            text = {
                Column {
                    for (fd in FirstDayOfWeek.values()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFirstDayOfWeek(fd)
                                    showFirstDayDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.appSettings.firstDayOfWeek == fd,
                                onClick = {
                                    viewModel.setFirstDayOfWeek(fd)
                                    showFirstDayDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = fd.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFirstDayDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Preferences
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Paid,
                            title = "Currency",
                            subtitle = "${uiState.appSettings.currency.symbol} - ${uiState.appSettings.currency.name}",
                            onClick = { showCurrencyDialog = true }
                        )

                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = "Theme",
                            subtitle = uiState.appSettings.themeMode.displayName,
                            onClick = { showThemeDialog = true }
                        )

                        SettingsRow(
                            icon = Icons.Default.DateRange,
                            title = "Date Format",
                            subtitle = uiState.appSettings.dateFormat.pattern,
                            onClick = { showDateFormatDialog = true }
                        )

                        SettingsRow(
                            icon = Icons.Default.DateRange,
                            title = "First Day of Week",
                            subtitle = uiState.appSettings.firstDayOfWeek.displayName,
                            onClick = { showFirstDayDialog = true }
                        )
                    }
                }
            }

            // Management Links
            item {
                Text(
                    text = "Manage Data Entities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Category,
                            title = "Manage Categories",
                            subtitle = "Customize expense & income categories",
                            onClick = onNavigateToCategories
                        )

                        SettingsRow(
                            icon = Icons.Default.AccountBalance,
                            title = "Manage Accounts & Wallets",
                            subtitle = "Configure Cash, Banks, UPI, Cards",
                            onClick = onNavigateToAccounts
                        )
                    }
                }
            }

            // Notifications & Reminders
            item {
                Text(
                    text = "Notifications & Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsSwitchRow(
                            icon = Icons.Default.Notifications,
                            title = "Daily Entry Reminder",
                            subtitle = "Reminder at 8:00 PM to record daily transactions",
                            checked = uiState.appSettings.dailyReminderEnabled,
                            onCheckedChange = { viewModel.toggleDailyReminder(it) }
                        )

                        SettingsSwitchRow(
                            icon = Icons.Default.Notifications,
                            title = "Budget Threshold Warnings",
                            subtitle = "Alert when spending reaches 80% and 100% of limits",
                            checked = uiState.appSettings.budgetAlertsEnabled,
                            onCheckedChange = { viewModel.toggleBudgetAlerts(it) }
                        )
                    }
                }
            }

            // Export, Import & Backup
            item {
                Text(
                    text = "Backup & Data Transfer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.TableChart,
                            title = "Export to CSV",
                            subtitle = "Share raw transactions table for Excel or Google Sheets",
                            onClick = { viewModel.exportTransactionsCsv(context) }
                        )

                        SettingsRow(
                            icon = Icons.Default.Upload,
                            title = "Import CSV File",
                            subtitle = "Import transactions from an external CSV file",
                            onClick = { csvPickerLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")) }
                        )

                        SettingsRow(
                            icon = Icons.Default.Backup,
                            title = "Backup to JSON",
                            subtitle = "Export full database backup including categories and accounts",
                            onClick = { viewModel.exportBackupJson(context) }
                        )

                        SettingsRow(
                            icon = Icons.Default.Restore,
                            title = "Restore from JSON",
                            subtitle = "Restore database state from a saved JSON backup file",
                            onClick = { jsonPickerLauncher.launch(arrayOf("application/json", "*/*")) }
                        )
                    }
                }
            }

            // Privacy & Offline Policy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "100% Offline & Private",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ExpenseMate stores all financial records exclusively on your local device. No telemetry, third-party analytics, or external server uploads.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // App Version & Reset Action
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "App Version",
                            subtitle = "ExpenseMate 1.0.0 (Production Release)",
                            onClick = {}
                        )

                        SettingsRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Reset All Application Data",
                            subtitle = "Erase all transactions, custom categories, accounts, and budgets",
                            titleColor = ExpenseRed,
                            onClick = { viewModel.requestResetData() }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == ExpenseRed) ExpenseRed else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
