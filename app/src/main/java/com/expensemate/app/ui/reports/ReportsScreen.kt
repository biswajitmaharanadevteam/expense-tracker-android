package com.expensemate.app.ui.reports

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensemate.app.domain.model.PeriodType
import com.expensemate.app.ui.components.CategoryDonutChart
import com.expensemate.app.ui.components.CategoryIconBadge
import com.expensemate.app.ui.components.IncomeExpenseBarChart
import com.expensemate.app.ui.components.MetricStatCard
import com.expensemate.app.ui.components.SpendingTrendLineChart
import com.expensemate.app.ui.components.TransactionRow
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen
import com.expensemate.app.ui.theme.SavingsTeal
import com.expensemate.app.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.shareEvent.collect { uri ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (uri.toString().endsWith(".pdf")) "application/pdf" else "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Financial Report"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.exportAndSharePdf(context) }) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF Report",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.exportAndShareCsv(context) }) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Export CSV",
                            tint = MaterialTheme.colorScheme.primary
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
                // Period Filters
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

                // Export buttons row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportAndSharePdf(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF")
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportAndShareCsv(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV")
                        }
                    }
                }

                // Overview Metrics (Income, Expense, Net Cash Flow, Average Daily)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricStatCard(
                            title = "Period Income",
                            amount = uiState.summary.periodIncome,
                            currency = uiState.appSettings.currency,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )

                        MetricStatCard(
                            title = "Period Expenses",
                            amount = uiState.summary.periodExpense,
                            currency = uiState.appSettings.currency,
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            accentColor = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricStatCard(
                            title = "Net Cash Flow",
                            amount = uiState.summary.netCashFlow,
                            currency = uiState.appSettings.currency,
                            icon = Icons.Default.Savings,
                            accentColor = if (uiState.summary.netCashFlow >= 0) IncomeGreen else ExpenseRed,
                            subtitle = if (uiState.summary.netCashFlow >= 0) "Surplus" else "Deficit",
                            modifier = Modifier.weight(1f)
                        )

                        MetricStatCard(
                            title = "Avg Daily Spend",
                            amount = uiState.summary.averageDailySpending,
                            currency = uiState.appSettings.currency,
                            icon = Icons.Default.CalendarMonth,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Period-over-period comparison card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Period-Over-Period Comparison",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Expense Change",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val expChange = uiState.periodComparison.expenseChangePercent
                                    val expColor = if (expChange > 0) ExpenseRed else IncomeGreen
                                    val sign = if (expChange > 0) "+" else ""
                                    Text(
                                        text = "$sign${String.format(java.util.Locale.US, "%.1f", expChange)}%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = expColor
                                    )
                                    Text(
                                        text = "vs preceding period (${CurrencyFormatter.format(uiState.periodComparison.previousPeriodExpense, uiState.appSettings.currency)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Income Change",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val incChange = uiState.periodComparison.incomeChangePercent
                                    val incColor = if (incChange >= 0) IncomeGreen else ExpenseRed
                                    val incSign = if (incChange > 0) "+" else ""
                                    Text(
                                        text = "$incSign${String.format(java.util.Locale.US, "%.1f", incChange)}%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = incColor
                                    )
                                    Text(
                                        text = "vs preceding period (${CurrencyFormatter.format(uiState.periodComparison.previousPeriodIncome, uiState.appSettings.currency)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Donut Chart - Category Breakdown
                item {
                    CategoryDonutChart(
                        categories = uiState.summary.topExpenseCategories,
                        totalExpense = uiState.summary.periodExpense,
                        currency = uiState.appSettings.currency
                    )
                }

                // Daily Spending Trend Chart
                item {
                    SpendingTrendLineChart(
                        trendData = uiState.summary.dailyTrend,
                        currency = uiState.appSettings.currency
                    )
                }

                // Monthly Comparison Bar Chart
                item {
                    IncomeExpenseBarChart(
                        data = uiState.monthlyComparisons,
                        currency = uiState.appSettings.currency
                    )
                }

                // Largest Transactions
                if (uiState.summary.largestTransactions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Largest Expenses This Period",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(uiState.summary.largestTransactions, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            currency = uiState.appSettings.currency,
                            onClick = {}
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
