package com.expensemate.app.ui.transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.ui.components.CategoryIconBadge
import com.expensemate.app.ui.components.ConfirmationDialog
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen
import com.expensemate.app.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: AddEditTransactionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCategorySheet by remember { mutableStateOf(false) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { success ->
            if (success) {
                onNavigateBack()
            }
        }
    }

    ConfirmationDialog(
        show = uiState.showDeleteConfirmation,
        title = "Delete Transaction",
        message = "Are you sure you want to delete this transaction permanently?",
        confirmButtonText = "Delete",
        onConfirm = { viewModel.confirmDelete() },
        onDismiss = { viewModel.dismissDeleteDialog() }
    )

    // Category Selection Modal Bottom Sheet
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                val filteredCats = uiState.categories.filter { it.type == uiState.type }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (cat in filteredCats) {
                        val isSelected = cat.id == uiState.selectedCategoryId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable {
                                    viewModel.onCategorySelected(cat.id)
                                    showCategorySheet = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = cat.iconName,
                                colorHex = cat.colorHex,
                                size = 32.dp,
                                iconSize = 18.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Transaction" else "Add Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(onClick = { viewModel.duplicateTransaction() }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate Transaction"
                            )
                        }
                        IconButton(onClick = { viewModel.showDeleteDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ExpenseRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Toggle (Expense / Income)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Expense Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (uiState.type == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
                        .clickable { viewModel.onTypeChanged(TransactionType.EXPENSE) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Income Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (uiState.type == TransactionType.INCOME) IncomeGreen else Color.Transparent)
                        .clickable { viewModel.onTypeChanged(TransactionType.INCOME) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.appSettings.currency.symbol,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = uiState.amountText,
                            onValueChange = { viewModel.onAmountChanged(it) },
                            placeholder = { Text("0.00", fontSize = 28.sp) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            isError = uiState.amountError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (uiState.amountError != null) {
                        Text(
                            text = uiState.amountError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            // Category Picker Row
            val selectedCategory = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategorySheet = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIconBadge(
                            iconName = selectedCategory?.iconName ?: "category",
                            colorHex = selectedCategory?.colorHex ?: "#2A3B90",
                            size = 40.dp,
                            iconSize = 22.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedCategory?.name ?: "Select Category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Title / Description Field
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Title / Description (optional)") },
                placeholder = { Text(selectedCategory?.name ?: "e.g. Dinner with friends") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Date Picker Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = uiState.date }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val updatedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                viewModel.onDateChanged(updatedCal.timeInMillis)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Transaction Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateUtils.formatDate(uiState.date, uiState.appSettings.dateFormat),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Account / Wallet Dropdown
            ExposedDropdownMenuBox(
                expanded = accountMenuExpanded,
                onExpandedChange = { accountMenuExpanded = it }
            ) {
                val selectedAccount = uiState.accounts.firstOrNull { it.id == uiState.selectedAccountId }
                OutlinedTextField(
                    value = selectedAccount?.name ?: "Select Account",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account / Wallet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = accountMenuExpanded,
                    onDismissRequest = { accountMenuExpanded = false }
                ) {
                    for (acc in uiState.accounts) {
                        DropdownMenuItem(
                            text = { Text(acc.name) },
                            onClick = {
                                viewModel.onAccountSelected(acc.id)
                                accountMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Payment Method Chips
            Text(text = "Payment Method", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (method in PaymentMethod.values()) {
                    FilterChip(
                        selected = uiState.selectedPaymentMethod == method,
                        onClick = { viewModel.onPaymentMethodSelected(method) },
                        label = { Text(method.displayName) }
                    )
                }
            }

            // Payee / Merchant
            OutlinedTextField(
                value = uiState.payeeOrMerchant,
                onValueChange = { viewModel.onPayeeChanged(it) },
                label = { Text("Payee / Merchant (optional)") },
                placeholder = { Text("e.g. Starbucks, Uber, Amazon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChanged(it) },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Additional details...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Tags
            OutlinedTextField(
                value = uiState.tagsText,
                onValueChange = { viewModel.onTagsChanged(it) },
                label = { Text("Tags (comma-separated, optional)") },
                placeholder = { Text("vacation, office, party") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isEditMode) "Update Transaction" else "Save Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
