package com.expensemate.app.ui.budgets

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensemate.app.domain.model.Budget
import com.expensemate.app.ui.components.BudgetStatusBanner
import com.expensemate.app.ui.components.CategoryIconBadge
import com.expensemate.app.ui.components.ConfirmationDialog
import com.expensemate.app.ui.components.EmptyState
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen
import com.expensemate.app.ui.theme.WarningAmber
import com.expensemate.app.util.CurrencyFormatter
import com.expensemate.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Confirmation for Delete Budget
    ConfirmationDialog(
        show = uiState.budgetToDelete != null,
        title = "Delete Budget",
        message = "Are you sure you want to delete this budget?",
        confirmButtonText = "Delete",
        onConfirm = { viewModel.confirmDeleteBudget() },
        onDismiss = { viewModel.dismissDeleteDialog() }
    )

    // Add / Edit Budget Dialog
    if (uiState.showAddEditDialog) {
        var selectedCatId by remember { mutableStateOf(uiState.budgetToEdit?.categoryId) }
        var limitText by remember {
            mutableStateOf(
                uiState.budgetToEdit?.amountLimit?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: ""
            )
        }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissAddEditDialog() },
            title = {
                Text(if (uiState.budgetToEdit != null) "Edit Budget" else "Create Budget")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Month: ${DateUtils.formatMonthYear(uiState.month, uiState.year)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        val currentCategoryName = if (selectedCatId == null) {
                            "Overall Monthly Budget"
                        } else {
                            uiState.categories.firstOrNull { it.id == selectedCatId }?.name ?: "Select Category"
                        }

                        OutlinedTextField(
                            value = currentCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Budget Target") },
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
                            DropdownMenuItem(
                                text = { Text("Overall Monthly Budget") },
                                onClick = {
                                    selectedCatId = null
                                    dropdownExpanded = false
                                }
                            )
                            for (cat in uiState.categories) {
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCatId = cat.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Budget Limit TextField
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = {
                            limitText = it
                            errorMsg = null
                        },
                        label = { Text("Spending Limit (${uiState.appSettings.currency.symbol})") },
                        placeholder = { Text("e.g. 15000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = errorMsg != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                        val limit = CurrencyFormatter.parseAmount(limitText)
                        if (limit == null || limit <= 0.0) {
                            errorMsg = "Please enter a valid amount greater than 0"
                        } else {
                            viewModel.saveBudget(selectedCatId, limit)
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
                title = { Text("Budgets", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddBudgetDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
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
            // Month Selector Bar (< March 2026 >)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }

                Text(
                    text = DateUtils.formatMonthYear(uiState.month, uiState.year),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Overall Budget Banner
                    if (uiState.overallBudget != null) {
                        item {
                            BudgetStatusBanner(
                                remainingAmount = uiState.overallBudget!!.remainingAmount,
                                spentAmount = uiState.overallBudget!!.amountSpent,
                                limitAmount = uiState.overallBudget!!.amountLimit,
                                currency = uiState.appSettings.currency
                            )
                        }
                    }

                    // Category Budgets Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category Budgets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Category Budgets List or Empty State
                    if (uiState.categoryBudgets.isEmpty() && uiState.overallBudget == null) {
                        item {
                            EmptyState(
                                title = "No budgets set for this month",
                                description = "Set spending limits for categories or an overall monthly limit to keep finances in check.",
                                icon = Icons.Default.PieChart,
                                actionButtonText = "Set a Budget",
                                onActionClick = { viewModel.openAddBudgetDialog() }
                            )
                        }
                    } else {
                        items(uiState.categoryBudgets, key = { it.id }) { budget ->
                            CategoryBudgetItem(
                                budget = budget,
                                currency = uiState.appSettings.currency,
                                onEdit = { viewModel.openEditBudgetDialog(budget) },
                                onDelete = { viewModel.requestDeleteBudget(budget) }
                            )
                        }
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
fun CategoryBudgetItem(
    budget: Budget,
    currency: com.expensemate.app.domain.model.CurrencyConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (budget.amountLimit > 0) ((budget.amountSpent / budget.amountLimit).toFloat()).coerceIn(0f, 1f) else 0f
    val progressColor = when {
        budget.isOverBudget -> ExpenseRed
        budget.percentageSpent >= 80f -> WarningAmber
        else -> IncomeGreen
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(
                    iconName = budget.categoryIcon,
                    colorHex = budget.categoryColor,
                    size = 36.dp,
                    iconSize = 20.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (budget.isOverBudget) "Over budget by ${CurrencyFormatter.format(budget.amountSpent - budget.amountLimit, currency)}"
                        else "${CurrencyFormatter.format(budget.remainingAmount, currency)} left",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (budget.isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )

                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = ExpenseRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${CurrencyFormatter.format(budget.amountSpent, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Limit: ${CurrencyFormatter.format(budget.amountLimit, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
