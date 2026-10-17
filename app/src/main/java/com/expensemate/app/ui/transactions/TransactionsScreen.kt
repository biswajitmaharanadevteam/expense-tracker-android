package com.expensemate.app.ui.transactions

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.TransactionSortOption
import com.expensemate.app.ui.components.ConfirmationDialog
import com.expensemate.app.ui.components.EmptyState
import com.expensemate.app.ui.components.TransactionRow
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen
import com.expensemate.app.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    ConfirmationDialog(
        show = uiState.transactionToDelete != null,
        title = "Delete Transaction",
        message = "Are you sure you want to delete '${uiState.transactionToDelete?.title}'? This action cannot be undone.",
        confirmButtonText = "Delete",
        onConfirm = { viewModel.confirmDeleteTransaction() },
        onDismiss = { viewModel.dismissDeleteDialog() }
    )

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.resetFilters() }) {
                        Text("Reset All")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction Type Chips
                Text(text = "Transaction Type", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.filterCriteria.type == null,
                        onClick = { viewModel.onTypeFilterChanged(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = uiState.filterCriteria.type == TransactionType.EXPENSE,
                        onClick = { viewModel.onTypeFilterChanged(TransactionType.EXPENSE) },
                        label = { Text("Expense") }
                    )
                    FilterChip(
                        selected = uiState.filterCriteria.type == TransactionType.INCOME,
                        onClick = { viewModel.onTypeFilterChanged(TransactionType.INCOME) },
                        label = { Text("Income") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sort Options
                Text(text = "Sort By", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (sort in TransactionSortOption.values()) {
                        FilterChip(
                            selected = uiState.filterCriteria.sortOption == sort,
                            onClick = { viewModel.onSortOptionChanged(sort) },
                            label = { Text(sort.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories
                if (uiState.categories.isNotEmpty()) {
                    Text(text = "Category", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filterCriteria.categoryId == null,
                            onClick = { viewModel.onCategoryFilterChanged(null) },
                            label = { Text("All Categories") }
                        )
                        for (cat in uiState.categories) {
                            FilterChip(
                                selected = uiState.filterCriteria.categoryId == cat.id,
                                onClick = { viewModel.onCategoryFilterChanged(cat.id) },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
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
        ) {
            // Search Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.filterCriteria.query,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search title, payee, category, notes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.filterCriteria.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Overview Summary Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.totalFilteredCount} transactions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiState.totalFilteredIncome > 0) {
                        Text(
                            text = "+${CurrencyFormatter.format(uiState.totalFilteredIncome, uiState.appSettings.currency)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                    if (uiState.totalFilteredExpense > 0) {
                        Text(
                            text = "-${CurrencyFormatter.format(uiState.totalFilteredExpense, uiState.appSettings.currency)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }

            // Transactions List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.groupedTransactions.isEmpty()) {
                EmptyState(
                    title = if (uiState.filterCriteria.query.isNotBlank()) "No matching transactions" else "No transactions yet",
                    description = if (uiState.filterCriteria.query.isNotBlank()) "Try refining your search terms or resetting filters." else "Start recording your expenses and income.",
                    actionButtonText = if (uiState.filterCriteria.query.isNotBlank()) "Reset Filters" else "Add Transaction",
                    onActionClick = {
                        if (uiState.filterCriteria.query.isNotBlank()) viewModel.resetFilters()
                        else onNavigateToAddTransaction()
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (group in uiState.groupedTransactions) {
                        item(key = "header_${group.dayMillis}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.dateLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val netDaily = group.totalIncome - group.totalExpense
                                Text(
                                    text = CurrencyFormatter.format(netDaily, uiState.appSettings.currency),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (netDaily >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }

                        items(group.transactions, key = { it.id }) { tx ->
                            TransactionRow(
                                transaction = tx,
                                currency = uiState.appSettings.currency,
                                onClick = { onNavigateToEditTransaction(tx.id) },
                                onEditClick = { onNavigateToEditTransaction(tx.id) },
                                onDeleteClick = { viewModel.requestDeleteTransaction(tx) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
