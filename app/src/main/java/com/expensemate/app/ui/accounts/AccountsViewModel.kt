package com.expensemate.app.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AccountType
import com.expensemate.app.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val appSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val accountToEdit: Account? = null,
    val accountToDelete: Account? = null,
    val deleteErrorMessage: String? = null
)

class AccountsViewModel(
    private val repository: ExpenseMateRepository
) : ViewModel() {

    private val _showAddEditDialog = MutableStateFlow(false)
    private val _accountToEdit = MutableStateFlow<Account?>(null)
    private val _accountToDelete = MutableStateFlow<Account?>(null)
    private val _deleteErrorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AccountsUiState> = combine(
        repository.getAccountsWithBalancesFlow(),
        repository.appSettingsFlow,
        _showAddEditDialog,
        _accountToEdit,
        _accountToDelete
    ) { accounts, settings, showDialog, toEdit, toDelete ->
        AccountsUiState(
            accounts = accounts,
            totalBalance = accounts.sumOf { it.currentBalance },
            appSettings = settings,
            isLoading = false,
            showAddEditDialog = showDialog,
            accountToEdit = toEdit,
            accountToDelete = toDelete,
            deleteErrorMessage = _deleteErrorMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountsUiState(isLoading = true)
    )

    fun openAddAccountDialog() {
        _accountToEdit.value = null
        _showAddEditDialog.value = true
    }

    fun openEditAccountDialog(account: Account) {
        _accountToEdit.value = account
        _showAddEditDialog.value = true
    }

    fun dismissAddEditDialog() {
        _showAddEditDialog.value = false
        _accountToEdit.value = null
    }

    fun requestDeleteAccount(account: Account) {
        viewModelScope.launch {
            val canDelete = repository.canDeleteAccount(account.id)
            if (!canDelete) {
                _deleteErrorMessage.value = "Cannot delete '${account.name}' because existing transactions are linked to it. Please reassign those transactions first."
            } else {
                _deleteErrorMessage.value = null
                _accountToDelete.value = account
            }
        }
    }

    fun dismissDeleteDialog() {
        _accountToDelete.value = null
        _deleteErrorMessage.value = null
    }

    fun confirmDeleteAccount() {
        val target = _accountToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteAccount(target.id)
            _accountToDelete.value = null
        }
    }

    fun saveAccount(name: String, type: AccountType, initialBalance: Double, colorHex: String) {
        viewModelScope.launch {
            val existing = _accountToEdit.value
            val account = Account(
                id = existing?.id ?: 0,
                name = name,
                type = type,
                initialBalance = initialBalance,
                colorHex = colorHex
            )
            if (existing != null) {
                repository.updateAccount(account)
            } else {
                repository.insertAccount(account)
            }
            dismissAddEditDialog()
        }
    }

    class Factory(private val repository: ExpenseMateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountsViewModel(repository) as T
        }
    }
}
