package com.expensemate.app.ui.transactions

import androidx.lifecycle.SavedStateHandle
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
import com.expensemate.app.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditTransactionUiState(
    val transactionId: Long? = null,
    val isEditMode: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val amountError: String? = null,
    val title: String = "",
    val titleError: String? = null,
    val selectedCategoryId: Long = 1,
    val selectedAccountId: Long = 1,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val date: Long = System.currentTimeMillis(),
    val payeeOrMerchant: String = "",
    val notes: String = "",
    val tagsText: String = "",
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)

class AddEditTransactionViewModel(
    private val repository: ExpenseMateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navTransactionId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it > 0 }
    private val initialTypeParam: String? = savedStateHandle.get<String>("type")

    private val _uiState = MutableStateFlow(
        AddEditTransactionUiState(
            transactionId = navTransactionId,
            isEditMode = navTransactionId != null,
            type = if (initialTypeParam?.uppercase() == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
        )
    )
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<Boolean>()
    val navigationEvents: SharedFlow<Boolean> = _navigationEvents.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val settings = repository.appSettingsFlow.first()
            val categories = repository.getAllCategoriesFlow().first()
            val accounts = repository.getAllAccountsFlow().first()

            val defaultCategoryId = if (_uiState.value.type == TransactionType.INCOME) {
                settings.defaultIncomeCategoryId
            } else {
                settings.defaultExpenseCategoryId
            }
            val defaultAccountId = settings.defaultAccountId

            if (navTransactionId != null) {
                val existing = repository.getTransactionById(navTransactionId)
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(
                        type = existing.type,
                        amountText = String.format(java.util.Locale.US, "%.2f", existing.amount),
                        title = existing.title,
                        selectedCategoryId = existing.categoryId,
                        selectedAccountId = existing.accountId,
                        selectedPaymentMethod = existing.paymentMethod,
                        date = existing.date,
                        payeeOrMerchant = existing.payeeOrMerchant,
                        notes = existing.notes,
                        tagsText = existing.tags.joinToString(", "),
                        categories = categories,
                        accounts = accounts,
                        appSettings = settings,
                        isLoading = false
                    )
                    return@launch
                }
            }

            // Fresh Transaction Defaults
            _uiState.value = _uiState.value.copy(
                selectedCategoryId = defaultCategoryId,
                selectedAccountId = defaultAccountId,
                categories = categories,
                accounts = accounts,
                appSettings = settings,
                isLoading = false
            )
        }
    }

    fun onTypeChanged(type: TransactionType) {
        val matchingCategory = _uiState.value.categories.firstOrNull { it.type == type }
        _uiState.value = _uiState.value.copy(
            type = type,
            selectedCategoryId = matchingCategory?.id ?: _uiState.value.selectedCategoryId
        )
    }

    fun onAmountChanged(amountText: String) {
        _uiState.value = _uiState.value.copy(
            amountText = amountText,
            amountError = null
        )
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = null
        )
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onAccountSelected(accountId: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }

    fun onDateChanged(timestamp: Long) {
        _uiState.value = _uiState.value.copy(date = timestamp)
    }

    fun onPayeeChanged(payee: String) {
        _uiState.value = _uiState.value.copy(payeeOrMerchant = payee)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun onTagsChanged(tags: String) {
        _uiState.value = _uiState.value.copy(tagsText = tags)
    }

    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
    }

    fun confirmDelete() {
        val id = navTransactionId ?: return
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _navigationEvents.emit(true)
        }
    }

    fun duplicateTransaction() {
        // Creates a new transaction with same details and current timestamp
        _uiState.value = _uiState.value.copy(
            transactionId = null,
            isEditMode = false,
            title = "${_uiState.value.title} (Copy)",
            date = System.currentTimeMillis()
        )
    }

    fun saveTransaction() {
        val state = _uiState.value

        val amount = CurrencyFormatter.parseAmount(state.amountText)
        if (amount == null || amount <= 0.0) {
            _uiState.value = state.copy(amountError = "Please enter a valid amount greater than 0")
            return
        }

        val title = if (state.title.isBlank()) {
            val cat = state.categories.firstOrNull { it.id == state.selectedCategoryId }
            cat?.name ?: "Transaction"
        } else {
            state.title.trim()
        }

        val tags = state.tagsText.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val category = state.categories.firstOrNull { it.id == state.selectedCategoryId }
        val account = state.accounts.firstOrNull { it.id == state.selectedAccountId }

        val transaction = Transaction(
            id = state.transactionId ?: 0,
            title = title,
            amount = amount,
            type = state.type,
            categoryId = state.selectedCategoryId,
            categoryName = category?.name ?: "",
            categoryIcon = category?.iconName ?: "category",
            categoryColor = category?.colorHex ?: "#2A3B90",
            accountId = state.selectedAccountId,
            accountName = account?.name ?: "",
            paymentMethod = state.selectedPaymentMethod,
            date = state.date,
            payeeOrMerchant = state.payeeOrMerchant.trim(),
            notes = state.notes.trim(),
            tags = tags
        )

        viewModelScope.launch {
            if (state.isEditMode && state.transactionId != null) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }
            _navigationEvents.emit(true)
        }
    }

    class Factory(
        private val repository: ExpenseMateRepository,
        private val savedStateHandle: SavedStateHandle
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditTransactionViewModel(repository, savedStateHandle) as T
        }
    }
}
