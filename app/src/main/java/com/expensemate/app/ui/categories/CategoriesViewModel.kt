package com.expensemate.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val selectedTab: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val showAddEditDialog: Boolean = false,
    val categoryToEdit: Category? = null,
    val categoryToDelete: Category? = null,
    val deleteErrorMessage: String? = null
)

class CategoriesViewModel(
    private val repository: ExpenseMateRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(TransactionType.EXPENSE)
    val selectedTab: StateFlow<TransactionType> = _selectedTab.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    private val _categoryToEdit = MutableStateFlow<Category?>(null)
    private val _categoryToDelete = MutableStateFlow<Category?>(null)
    private val _deleteErrorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        _selectedTab,
        repository.getAllCategoriesFlow(),
        _showAddEditDialog,
        _categoryToEdit,
        _categoryToDelete
    ) { tab, allCats, showDialog, toEdit, toDelete ->
        val filtered = allCats.filter { it.type == tab }
        CategoriesUiState(
            selectedTab = tab,
            categories = filtered,
            showAddEditDialog = showDialog,
            categoryToEdit = toEdit,
            categoryToDelete = toDelete,
            deleteErrorMessage = _deleteErrorMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesUiState()
    )

    fun selectTab(tab: TransactionType) {
        _selectedTab.value = tab
    }

    fun openAddCategoryDialog() {
        _categoryToEdit.value = null
        _showAddEditDialog.value = true
    }

    fun openEditCategoryDialog(category: Category) {
        _categoryToEdit.value = category
        _showAddEditDialog.value = true
    }

    fun dismissAddEditDialog() {
        _showAddEditDialog.value = false
        _categoryToEdit.value = null
    }

    fun requestDeleteCategory(category: Category) {
        viewModelScope.launch {
            val canDelete = repository.canDeleteCategory(category.id)
            if (!canDelete) {
                _deleteErrorMessage.value = "Cannot delete category '${category.name}' because transactions are currently assigned to it. Reassign or delete those transactions first."
            } else {
                _deleteErrorMessage.value = null
                _categoryToDelete.value = category
            }
        }
    }

    fun dismissDeleteDialog() {
        _categoryToDelete.value = null
        _deleteErrorMessage.value = null
    }

    fun confirmDeleteCategory() {
        val target = _categoryToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteCategory(target.id)
            _categoryToDelete.value = null
        }
    }

    fun saveCategory(name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val existing = _categoryToEdit.value
            val category = Category(
                id = existing?.id ?: 0,
                name = name,
                type = _selectedTab.value,
                iconName = iconName,
                colorHex = colorHex,
                isDefault = existing?.isDefault ?: false
            )
            if (existing != null) {
                repository.updateCategory(category)
            } else {
                repository.insertCategory(category)
            }
            dismissAddEditDialog()
        }
    }

    class Factory(private val repository: ExpenseMateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoriesViewModel(repository) as T
        }
    }
}
