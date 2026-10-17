package com.expensemate.app.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensemate.app.domain.model.PeriodType
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.ui.components.CategoryDonutChart
import com.expensemate.app.ui.components.CategoryIconBadge
import com.expensemate.app.ui.components.EmptyState
import com.expensemate.app.ui.components.IncomeExpenseBarChart
import com.expensemate.app.ui.components.MetricStatCard
import com.expensemate.app.ui.components.TotalBalanceCard
import com.expensemate.app.ui.components.TransactionRow
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen
import com.expensemate.app.ui.theme.SavingsTeal
import com.expensemate.app.util.CurrencyFormatter
import com.expensemate.app.util.WindowType
import com.expensemate.app.util.rememberWindowSizeInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddTransaction: (String) -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowInfo = rememberWindowSizeInfo()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ExpenseMate",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Smart Financial Tracker",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAccounts) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Accounts"
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Selection Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PeriodType.values().filter { it != PeriodType.CUSTOM }) { period ->
                            val selected = uiState.selectedPeriod == period
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.selectPeriod(period) },
                                label = { Text(period.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Total Balance & Net Cash Flow Card
                item {
                    TotalBalanceCard(
                        balance = uiState.summary.totalBalance,
                        periodIncome = uiState.summary.periodIncome,
                        periodExpense = uiState.summary.periodExpense,
                        currency = uiState.appSettings.currency
                    )
                }

                // Quick Action Buttons (Add Income, Add Expense, Manage Budgets)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToAddTransaction("INCOME") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Income", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = { onNavigateToAddTransaction("EXPENSE") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Expense", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onNavigateToBudgets,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Budgets", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Adaptive Grid / Row of Financial Metrics
                item {
                    val isWide = windowInfo.widthType != WindowType.COMPACT
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricStatCard(
                                title = "Net Cash Flow",
                                amount = uiState.summary.netCashFlow,
                                currency = uiState.appSettings.currency,
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                accentColor = if (uiState.summary.netCashFlow >= 0) IncomeGreen else ExpenseRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Period Savings",
                                amount = uiState.summary.totalSavings,
                                currency = uiState.appSettings.currency,
                                icon = Icons.Default.Savings,
                                accentColor = SavingsTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricStatCard(
                                title = "Net Cash Flow",
                                amount = uiState.summary.netCashFlow,
                                currency = uiState.appSettings.currency,
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                accentColor = if (uiState.summary.netCashFlow >= 0) IncomeGreen else ExpenseRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Period Savings",
                                amount = uiState.summary.totalSavings,
                                currency = uiState.appSettings.currency,
                                icon = Icons.Default.Savings,
                                accentColor = SavingsTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Income vs Expenses Monthly Chart
                item {
                    IncomeExpenseBarChart(
                        data = uiState.monthlyComparisons,
                        currency = uiState.appSettings.currency
                    )
                }

                // Category Spending Donut Chart
                item {
                    CategoryDonutChart(
                        categories = uiState.summary.topExpenseCategories,
                        totalExpense = uiState.summary.periodExpense,
                        currency = uiState.appSettings.currency
                    )
                }

                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (uiState.recentTransactions.isNotEmpty()) {
                            TextButton(onClick = onNavigateToTransactions) {
                                Text("View All")
                            }
                        }
                    }
                }

                // Recent Transactions List or Empty State
                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No transactions yet",
                            description = "Add your first transaction to start tracking your finances.",
                            actionButtonText = "Add Transaction",
                            onActionClick = { onNavigateToAddTransaction("EXPENSE") }
                        )
                    }
                } else {
                    items(uiState.recentTransactions, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            currency = uiState.appSettings.currency,
                            onClick = { onNavigateToEditTransaction(tx.id) }
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
