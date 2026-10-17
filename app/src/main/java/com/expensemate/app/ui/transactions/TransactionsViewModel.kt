package com.expensemate.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.FilterTransactionsUseCase
import com.expensemate.app.domain.usecase.TransactionFilterCriteria
import com.expensemate.app.domain.usecase.TransactionSortOption
import com.expensemate.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupedDailyTransactions(
    val dateLabel: String,
    val dayMillis: Long,
    val totalExpense: Double,
    val totalIncome: Double,
    val transactions: List<Transaction>
)

data class TransactionsUiState(
    val filterCriteria: TransactionFilterCriteria = TransactionFilterCriteria(),
    val groupedTransactions: List<GroupedDailyTransactions> = emptyList(),
    val totalFilteredCount: Int = 0,
    val totalFilteredExpense: Double = 0.0,
    val totalFilteredIncome: Double = 0.0,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val transactionToDelete: Transaction? = null
)

class TransactionsViewModel(
    private val repository: ExpenseMateRepository,
    private val filterUseCase: FilterTransactionsUseCase = FilterTransactionsUseCase()
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(TransactionFilterCriteria())
    val filterCriteria: StateFlow<TransactionFilterCriteria> = _filterCriteria.asStateFlow()

    private val _transactionToDelete = MutableStateFlow<Transaction?>(null)
    val transactionToDelete: StateFlow<Transaction?> = _transactionToDelete.asStateFlow()

    val uiState: StateFlow<TransactionsUiState> = combine(
        _filterCriteria,
        _transactionToDelete,
        repository.getAllTransactionsFlow(),
        repository.getAllCategoriesFlow(),
        repository.getAllAccountsFlow(),
        repository.appSettingsFlow
    ) { criteria, toDelete, allTx, categories, accounts, settings ->
        val filtered = filterUseCase(allTx, criteria)

        val totalExpense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalIncome = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        // Group by day
        val dayGroups = filtered.groupBy { DateUtils.getStartOfDay(it.date) }
        val groupedList = dayGroups.map { (dayMillis, txList) ->
            GroupedDailyTransactions(
                dateLabel = DateUtils.formatRelativeDate(dayMillis),
                dayMillis = dayMillis,
                totalExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                totalIncome = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                transactions = txList
            )
        }.sortedByDescending { it.dayMillis }

        TransactionsUiState(
            filterCriteria = criteria,
            groupedTransactions = groupedList,
            totalFilteredCount = filtered.size,
            totalFilteredExpense = totalExpense,
            totalFilteredIncome = totalIncome,
            categories = categories,
            accounts = accounts,
            appSettings = settings,
            isLoading = false,
            transactionToDelete = toDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun onTypeFilterChanged(type: TransactionType?) {
        _filterCriteria.value = _filterCriteria.value.copy(type = type)
    }

    fun onCategoryFilterChanged(categoryId: Long?) {
        _filterCriteria.value = _filterCriteria.value.copy(categoryId = categoryId)
    }

    fun onAccountFilterChanged(accountId: Long?) {
        _filterCriteria.value = _filterCriteria.value.copy(accountId = accountId)
    }

    fun onPaymentMethodFilterChanged(method: PaymentMethod?) {
        _filterCriteria.value = _filterCriteria.value.copy(paymentMethod = method)
    }

    fun onSortOptionChanged(sort: TransactionSortOption) {
        _filterCriteria.value = _filterCriteria.value.copy(sortOption = sort)
    }

    fun resetFilters() {
        _filterCriteria.value = TransactionFilterCriteria()
    }

    fun requestDeleteTransaction(transaction: Transaction) {
        _transactionToDelete.value = transaction
    }

    fun dismissDeleteDialog() {
        _transactionToDelete.value = null
    }

    fun confirmDeleteTransaction() {
        val target = _transactionToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteTransaction(target.id)
            _transactionToDelete.value = null
        }
    }

    class Factory(private val repository: ExpenseMateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionsViewModel(repository) as T
        }
    }
}
