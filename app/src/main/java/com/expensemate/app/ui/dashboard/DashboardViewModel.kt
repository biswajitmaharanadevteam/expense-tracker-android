package com.expensemate.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.FinancialSummary
import com.expensemate.app.domain.model.MonthlyComparison
import com.expensemate.app.domain.model.PeriodType
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.CalculateFinancialSummaryUseCase
import com.expensemate.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val selectedPeriod: PeriodType = PeriodType.THIS_MONTH,
    val summary: FinancialSummary = FinancialSummary(),
    val recentTransactions: List<Transaction> = emptyList(),
    val monthlyComparisons: List<MonthlyComparison> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: ExpenseMateRepository,
    private val calculateSummaryUseCase: CalculateFinancialSummaryUseCase = CalculateFinancialSummaryUseCase()
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(PeriodType.THIS_MONTH)
    val selectedPeriod: StateFlow<PeriodType> = _selectedPeriod.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedPeriod,
        repository.appSettingsFlow,
        repository.getAllAccountsFlow(),
        repository.getAllTransactionsFlow()
    ) { period, settings, accounts, allTransactions ->
        val dateRange = DateUtils.getDateRangeForPeriod(period, settings.firstDayOfWeek)
        val periodTransactions = allTransactions.filter { it.date in dateRange.startMillis..dateRange.endMillis }

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        // Budgets for current month
        val summary = calculateSummaryUseCase(
            periodTransactions = periodTransactions,
            allAccounts = accounts,
            budgetsForPeriod = emptyList(), // Filled reactively or from db
            dateRange = dateRange
        )

        // Calculate last 4 months comparison for chart
        val monthlyComparisons = mutableListOf<MonthlyComparison>()
        for (i in 3 downTo 0) {
            val mCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, -i)
            }
            val m = mCal.get(Calendar.MONTH) + 1
            val y = mCal.get(Calendar.YEAR)
            val (mStart, mEnd) = DateUtils.getMonthStartAndEnd(m, y)
            val mTx = allTransactions.filter { it.date in mStart..mEnd }
            val income = mTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = mTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            monthlyComparisons.add(
                MonthlyComparison(
                    monthLabel = DateUtils.formatShortMonth(m),
                    month = m,
                    year = y,
                    incomeAmount = income,
                    expenseAmount = expense
                )
            )
        }

        DashboardUiState(
            selectedPeriod = period,
            summary = summary,
            recentTransactions = allTransactions.take(5),
            monthlyComparisons = monthlyComparisons,
            appSettings = settings,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun selectPeriod(period: PeriodType) {
        _selectedPeriod.value = period
    }

    class Factory(private val repository: ExpenseMateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
