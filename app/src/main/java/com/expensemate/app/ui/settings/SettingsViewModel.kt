package com.expensemate.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.DateFormatOption
import com.expensemate.app.domain.model.FirstDayOfWeek
import com.expensemate.app.domain.model.ThemeMode
import com.expensemate.app.export.BackupData
import com.expensemate.app.export.CsvExporter
import com.expensemate.app.export.CsvImporter
import com.expensemate.app.export.ImportResult
import com.expensemate.app.export.JsonBackupManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val isResetting: Boolean = false,
    val showResetConfirmation: Boolean = false,
    val importResult: ImportResult? = null,
    val statusMessage: String? = null
)

class SettingsViewModel(
    private val repository: ExpenseMateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = repository.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        ).let { settingsFlow ->
            _uiState.apply {
                viewModelScope.launch {
                    settingsFlow.collect { settings ->
                        _uiState.value = _uiState.value.copy(appSettings = settings)
                    }
                }
            }.asStateFlow()
        }

    private val _shareEvent = MutableSharedFlow<Uri>()
    val shareEvent: SharedFlow<Uri> = _shareEvent.asSharedFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setCurrency(currency: CurrencyConfig) {
        viewModelScope.launch { repository.setCurrency(currency) }
    }

    fun setFirstDayOfWeek(firstDay: FirstDayOfWeek) {
        viewModelScope.launch { repository.setFirstDayOfWeek(firstDay) }
    }

    fun setDateFormat(format: DateFormatOption) {
        viewModelScope.launch { repository.setDateFormat(format) }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        val s = uiState.value.appSettings
        viewModelScope.launch { repository.setDailyReminder(enabled, s.dailyReminderHour, s.dailyReminderMinute) }
    }

    fun toggleBudgetAlerts(enabled: Boolean) {
        viewModelScope.launch { repository.setBudgetAlertsEnabled(enabled) }
    }

    fun toggleRecurringAlerts(enabled: Boolean) {
        viewModelScope.launch { repository.setRecurringAlertsEnabled(enabled) }
    }

    fun requestResetData() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = true)
    }

    fun dismissResetDialog() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = false)
    }

    fun confirmResetData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResetting = true, showResetConfirmation = false)
            repository.resetAllData()
            _uiState.value = _uiState.value.copy(
                isResetting = false,
                statusMessage = "All application data and preferences have been successfully reset."
            )
        }
    }

    fun dismissStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, importResult = null)
    }

    fun exportTransactionsCsv(context: Context) {
        viewModelScope.launch {
            val transactions = repository.getAllTransactionsFlow().first()
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "ExpenseMate_All_Transactions_${System.currentTimeMillis()}.csv")

            FileOutputStream(file).use { out ->
                CsvExporter.exportTransactions(transactions, out)
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            _shareEvent.emit(uri)
        }
    }

    fun exportBackupJson(context: Context) {
        viewModelScope.launch {
            val transactions = repository.getAllTransactionsFlow().first()
            val categories = repository.getAllCategoriesFlow().first()
            val accounts = repository.getAllAccountsFlow().first()

            val backup = BackupData(
                exportTimestamp = System.currentTimeMillis(),
                appVersion = "1.0.0",
                transactions = transactions,
                categories = categories,
                accounts = accounts,
                budgets = emptyList(),
                recurringRules = emptyList()
            )

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "ExpenseMate_Full_Backup_${System.currentTimeMillis()}.json")

            FileOutputStream(file).use { out ->
                JsonBackupManager.exportBackup(backup, out)
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            _shareEvent.emit(uri)
        }
    }

    fun importCsv(inputStream: InputStream) {
        viewModelScope.launch {
            val categories = repository.getAllCategoriesFlow().first()
            val accounts = repository.getAllAccountsFlow().first()

            val result = CsvImporter.importTransactions(inputStream, categories, accounts)
            if (result.importedTransactions.isNotEmpty()) {
                repository.insertTransactions(result.importedTransactions)
            }

            _uiState.value = _uiState.value.copy(
                importResult = result,
                statusMessage = "Import completed: ${result.importedTransactions.size} transactions added (${result.skippedRowCount} skipped)."
            )
        }
    }

    fun restoreBackupJson(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val backup = JsonBackupManager.parseBackup(inputStream)
                if (backup.transactions.isNotEmpty()) {
                    repository.insertTransactions(backup.transactions)
                }
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Successfully restored ${backup.transactions.size} transactions from backup file."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Error restoring backup: ${e.message}"
                )
            }
        }
    }

    class Factory(private val repository: ExpenseMateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
