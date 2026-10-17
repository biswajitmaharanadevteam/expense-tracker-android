package com.expensemate.app.ui.accounts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AccountType
import com.expensemate.app.ui.components.ConfirmationDialog
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen
import com.expensemate.app.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Delete Confirmation
    ConfirmationDialog(
        show = uiState.accountToDelete != null,
        title = "Delete Account",
        message = "Are you sure you want to delete '${uiState.accountToDelete?.name}'?",
        confirmButtonText = "Delete",
        onConfirm = { viewModel.confirmDeleteAccount() },
        onDismiss = { viewModel.dismissDeleteDialog() }
    )

    // Cannot Delete Alert Dialog
    if (uiState.deleteErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Account In Use") },
            text = { Text(uiState.deleteErrorMessage ?: "") },
            confirmButton = {
                Button(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    // Add / Edit Account Dialog
    if (uiState.showAddEditDialog) {
        var nameText by remember { mutableStateOf(uiState.accountToEdit?.name ?: "") }
        var typeSelected by remember { mutableStateOf(uiState.accountToEdit?.type ?: AccountType.CASH) }
        var initialBalanceText by remember {
            mutableStateOf(
                uiState.accountToEdit?.initialBalance?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "0.00"
            )
        }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissAddEditDialog() },
            title = { Text(if (uiState.accountToEdit != null) "Edit Account" else "Add Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = {
                            nameText = it
                            errorMsg = null
                        },
                        label = { Text("Account Name") },
                        placeholder = { Text("e.g. HDFC Salary, Main Cash") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = typeSelected.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            for (type in AccountType.values()) {
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        typeSelected = type
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Initial Balance Field
                    OutlinedTextField(
                        value = initialBalanceText,
                        onValueChange = { initialBalanceText = it },
                        label = { Text("Initial / Opening Balance (${uiState.appSettings.currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isBlank()) {
                            errorMsg = "Account name cannot be empty"
                        } else {
                            val initBal = CurrencyFormatter.parseAmount(initialBalanceText) ?: 0.0
                            val color = when (typeSelected) {
                                AccountType.CASH -> "#10B981"
                                AccountType.BANK -> "#2A3B90"
                                AccountType.UPI -> "#0EA5E9"
                                AccountType.CREDIT_CARD -> "#EC4899"
                                AccountType.SAVINGS -> "#00897B"
                                AccountType.CUSTOM -> "#8B5CF6"
                            }
                            viewModel.saveAccount(nameText.trim(), typeSelected, initBal, color)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissAddEditDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts & Wallets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddAccountDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Balance Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Net Liquid Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.format(uiState.totalBalance, uiState.appSettings.currency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.accounts, key = { it.id }) { account ->
                        AccountItemRow(
                            account = account,
                            currency = uiState.appSettings.currency,
                            onEdit = { viewModel.openEditAccountDialog(account) },
                            onDelete = { viewModel.requestDeleteAccount(account) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AccountItemRow(
    account: Account,
    currency: com.expensemate.app.domain.model.CurrencyConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (account.type) {
        AccountType.CASH -> Icons.Default.Money
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.UPI -> Icons.Default.PhoneAndroid
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
        AccountType.SAVINGS -> Icons.Default.Savings
        AccountType.CUSTOM -> Icons.Default.AccountBalanceWallet
    }

    val color = try {
        Color(android.graphics.Color.parseColor(account.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${account.type.displayName} • Opening: ${CurrencyFormatter.format(account.initialBalance, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(account.currentBalance, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (account.currentBalance >= 0) IncomeGreen else ExpenseRed
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (!account.isDefault) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = ExpenseRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
