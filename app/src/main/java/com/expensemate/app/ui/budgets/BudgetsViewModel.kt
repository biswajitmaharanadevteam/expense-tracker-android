package com.expensemate.app.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.Budget
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.BudgetProgress
import com.expensemate.app.domain.usecase.BudgetProgressUseCase
import com.expensemate.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetsUiState(
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val overallBudget: Budget? = null,
    val categoryBudgets: List<Budget> = emptyList(),
    val totalBudgetLimit: Double = 0.0,
    val totalBudgetSpent: Double = 0.0,
    val categories: List<Category> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val budgetToDelete: Budget? = null,
    val budgetToEdit: Budget? = null,
    val showAddEditDialog: Boolean = false
)

class BudgetsViewModel(
    private val repository: ExpenseMateRepository,
    private val budgetProgressUseCase: BudgetProgressUseCase = BudgetProgressUseCase()
) : ViewModel() {

    private val currentCal = Calendar.getInstance()
    private val _month = MutableStateFlow(currentCal.get(Calendar.MONTH) + 1)
    private val _year = MutableStateFlow(currentCal.get(Calendar.YEAR))

    private val _budgetToDelete = MutableStateFlow<Budget?>(null)
    private val _budgetToEdit = MutableStateFlow<Budget?>(null)
    private val _showAddEditDialog = MutableStateFlow(false)

    val uiState: StateFlow<BudgetsUiState> = combine(
        _month,
        _year,
        _budgetToDelete,
        _budgetToEdit,
        _showAddEditDialog
    ) { month, year, toDelete, toEdit, showDialog ->
        BudgetDataHolder(month, year, toDelete, toEdit, showDialog)
    }.combine(repository.appSettingsFlow) { holder, settings ->
        Pair(holder, settings)
    }.combine(repository.getAllCategoriesFlow()) { pair, categories ->
        Triple(pair.first, pair.second, categories)
    }.combine(
        combine(_month, _year) { m, y -> repository.getBudgetsForMonthFlow(m, y) }.first()
    ) { triple, budgets ->
        val holder = triple.first
        val settings = triple.second
        val categories = triple.third

        val overall = budgets.firstOrNull { it.categoryId == null }
        val categoryBudgets = budgets.filter { it.categoryId != null }

        val totalLimit = overall?.amountLimit ?: categoryBudgets.sumOf { it.amountLimit }
        val totalSpent = overall?.amountSpent ?: categoryBudgets.sumOf { it.amountSpent }

        BudgetsUiState(
            month = holder.month,
            year = holder.year,
            overallBudget = overall,
            categoryBudgets = categoryBudgets,
            totalBudgetLimit = totalLimit,
            totalBudgetSpent = totalSpent,
            categories = categories.filter { it.type == TransactionType.EXPENSE },
            appSettings = settings,
            isLoading = false,
            budgetToDelete = holder.toDelete,
            budgetToEdit = holder.toEdit,
            showAddEditDialog = holder.showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsUiState(isLoading = true)
    )

    fun previousMonth() {
        if (_month.value == 1) {
            _month.value = 12
            _year.value -= 1
        } else {
            _month.value -= 1
        }
    }

    fun nextMonth() {
        if (_month.value == 12) {
            _month.value = 1
            _year.value += 1
        } else {
            _month.value += 1
        }
    }

    fun openAddBudgetDialog() {
        _budgetToEdit.value = null
        _showAddEditDialog.value = true
    }

    fun openEditBudgetDialog(budget: Budget) {
        _budgetToEdit.value = budget
        _showAddEditDialog.value = true
    }

    fun dismissAddEditDialog() {
        _showAddEditDialog.value = false
        _budgetToEdit.value = null
    }

    fun requestDeleteBudget(budget: Budget) {
        _budgetToDelete.value = budget
    }

    fun dismissDeleteDialog() {
        _budgetToDelete.value = null
    }

    fun confirmDeleteBudget() {
        val target = _budgetToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteBudget(target.id)
            _budgetToDelete.value = null
        }
    }

    fun saveBudget(categoryId: Long?, limitAmount: Double) {
        val month = _month.value
        val year = _year.value
        val existing = _budgetToEdit.value

        viewModelScope.launch {
            val budget = Budget(
                id = existing?.id ?: 0,
                categoryId = categoryId,
                month = month,
                year = year,
                amountLimit = limitAmount
            )
            if (existing != null) {
                repository.updateBudget(budget)
            } else {
                repository.insertBudget(budget)
            }
            dismissAddEditDialog()
        }
    }

    private data class BudgetDataHolder(
        val month: Int,
        val year: Int,
        val toDelete: Budget?,
        val toEdit: Budget?,
        val showDialog: Boolean
    )

    class Factory(private val repository: ExpenseMateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BudgetsViewModel(repository) as T
        }
    }
}
