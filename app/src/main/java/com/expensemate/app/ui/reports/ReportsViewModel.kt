package com.expensemate.app.ui.reports

import android.content.Context
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.DateRange
import com.expensemate.app.domain.model.FinancialSummary
import com.expensemate.app.domain.model.MonthlyComparison
import com.expensemate.app.domain.model.PeriodType
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.CalculateFinancialSummaryUseCase
import com.expensemate.app.export.CsvExporter
import com.expensemate.app.export.PdfReportExporter
import com.expensemate.app.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

data class PeriodOverPeriodStats(
    val previousPeriodExpense: Double = 0.0,
    val previousPeriodIncome: Double = 0.0,
    val expenseChangePercent: Float = 0f,
    val incomeChangePercent: Float = 0f
)

data class ReportsUiState(
    val selectedPeriod: PeriodType = PeriodType.THIS_MONTH,
    val dateRange: DateRange = DateUtils.getDateRangeForPeriod(PeriodType.THIS_MONTH),
    val summary: FinancialSummary = FinancialSummary(),
    val periodTransactions: List<Transaction> = emptyList(),
    val periodComparison: PeriodOverPeriodStats = PeriodOverPeriodStats(),
    val monthlyComparisons: List<MonthlyComparison> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = false
)

class ReportsViewModel(
    private val repository: ExpenseMateRepository,
    private val calculateSummaryUseCase: CalculateFinancialSummaryUseCase = CalculateFinancialSummaryUseCase()
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(PeriodType.THIS_MONTH)
    val selectedPeriod: StateFlow<PeriodType> = _selectedPeriod.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)

    private val _shareEvent = MutableSharedFlow<android.net.Uri>()
    val shareEvent: SharedFlow<android.net.Uri> = _shareEvent.asSharedFlow()

    val uiState: StateFlow<ReportsUiState> = combine(
        _selectedPeriod,
        _customDateRange,
        repository.appSettingsFlow,
        repository.getAllTransactionsFlow(),
        repository.getAllAccountsFlow()
    ) { period, customRange, settings, allTx, accounts ->
        val dateRange = DateUtils.getDateRangeForPeriod(
            periodType = period,
            firstDayOfWeek = settings.firstDayOfWeek,
            customStart = customRange?.first,
            customEnd = customRange?.second
        )

        val periodTx = allTx.filter { it.date in dateRange.startMillis..dateRange.endMillis }
        val summary = calculateSummaryUseCase(periodTx, accounts, emptyList(), dateRange)

        // Calculate Preceding Period Comparison
        val periodDuration = dateRange.endMillis - dateRange.startMillis
        val prevStart = dateRange.startMillis - periodDuration
        val prevEnd = dateRange.startMillis - 1
        val prevTx = allTx.filter { it.date in prevStart..prevEnd }

        val prevExpense = prevTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val prevIncome = prevTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        val expenseChangePct = if (prevExpense > 0) {
            (((summary.periodExpense - prevExpense) / prevExpense) * 100).toFloat()
        } else 0f

        val incomeChangePct = if (prevIncome > 0) {
            (((summary.periodIncome - prevIncome) / prevIncome) * 100).toFloat()
        } else 0f

        // Monthly comparison breakdown (past 6 months)
        val monthlyComparisons = mutableListOf<MonthlyComparison>()
        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val m = cal.get(Calendar.MONTH) + 1
            val y = cal.get(Calendar.YEAR)
            val (mStart, mEnd) = DateUtils.getMonthStartAndEnd(m, y)
            val mTx = allTx.filter { it.date in mStart..mEnd }
            monthlyComparisons.add(
                MonthlyComparison(
                    monthLabel = DateUtils.formatShortMonth(m),
                    month = m,
                    year = y,
                    incomeAmount = mTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expenseAmount = mTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                )
            )
        }

        ReportsUiState(
            selectedPeriod = period,
            dateRange = dateRange,
            summary = summary,
            periodTransactions = periodTx,
            periodComparison = PeriodOverPeriodStats(
                previousPeriodExpense = prevExpense,
                previousPeriodIncome = prevIncome,
                expenseChangePercent = expenseChangePct,
                incomeChangePercent = incomeChangePct
            ),
            monthlyComparisons = monthlyComparisons,
            appSettings = settings,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    fun selectPeriod(period: PeriodType) {
        _selectedPeriod.value = period
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _customDateRange.value = Pair(start, end)
        _selectedPeriod.value = PeriodType.CUSTOM
    }

    fun exportAndSharePdf(context: Context) {
        viewModelScope.launch {
            val state = uiState.value
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val file = File(reportsDir, "ExpenseMate_Report_${System.currentTimeMillis()}.pdf")

            FileOutputStream(file).use { out ->
                PdfReportExporter.generatePdfReport(
                    summary = state.summary,
                    transactions = state.periodTransactions,
                    periodTitle = state.dateRange.label,
                    currency = state.appSettings.currency,
                    outputStream = out
                )
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            _shareEvent.emit(uri)
        }
    }

    fun exportAndShareCsv(context: Context) {
        viewModelScope.launch {
            val state = uiState.value
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "ExpenseMate_Transactions_${System.currentTimeMillis()}.csv")

            FileOutputStream(file).use { out ->
                CsvExporter.exportTransactions(state.periodTransactions, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            _shareEvent.emit(uri)
        }
    }

    class Factory(private val repository: ExpenseMateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(repository) as T
        }
    }
}
