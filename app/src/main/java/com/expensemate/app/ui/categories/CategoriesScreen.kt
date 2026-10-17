package com.expensemate.app.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.ui.components.CategoryIconBadge
import com.expensemate.app.ui.components.ConfirmationDialog
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen

private val AVAILABLE_COLORS = listOf(
    "#2A3B90", "#10B981", "#EF4444", "#F59E0B", "#00897B",
    "#8B5CF6", "#EC4899", "#06B6D4", "#F97316", "#64748B"
)

private val AVAILABLE_ICONS = listOf(
    "restaurant", "shopping_cart", "directions_car", "local_gas_station",
    "shopping_bag", "home", "bolt", "wifi", "local_hospital", "school",
    "movie", "flight", "subscriptions", "spa", "payments", "laptop_mac",
    "storefront", "trending_up", "account_balance", "card_giftcard", "savings"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Confirmation for Delete
    ConfirmationDialog(
        show = uiState.categoryToDelete != null,
        title = "Delete Category",
        message = "Are you sure you want to delete '${uiState.categoryToDelete?.name}'?",
        confirmButtonText = "Delete",
        onConfirm = { viewModel.confirmDeleteCategory() },
        onDismiss = { viewModel.dismissDeleteDialog() }
    )

    // In-Use Warning Alert Dialog
    if (uiState.deleteErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Category In Use") },
            text = { Text(uiState.deleteErrorMessage ?: "") },
            confirmButton = {
                Button(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    // Add / Edit Dialog
    if (uiState.showAddEditDialog) {
        var nameText by remember { mutableStateOf(uiState.categoryToEdit?.name ?: "") }
        var iconSelected by remember { mutableStateOf(uiState.categoryToEdit?.iconName ?: "category") }
        var colorSelected by remember { mutableStateOf(uiState.categoryToEdit?.colorHex ?: "#2A3B90") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissAddEditDialog() },
            title = { Text(if (uiState.categoryToEdit != null) "Edit Category" else "Add Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = {
                            nameText = it
                            errorMsg = null
                        },
                        label = { Text("Category Name") },
                        placeholder = { Text("e.g. Gym & Fitness") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Color Picker Row
                    Text(text = "Choose Color", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (colorHex in AVAILABLE_COLORS) {
                            val color = try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (e: Exception) {
                                Color.Gray
                            }
                            val isSelected = colorSelected.equals(colorHex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorSelected = colorHex }
                            )
                        }
                    }

                    // Icon Picker Row
                    Text(text = "Choose Icon", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (iconName in AVAILABLE_ICONS.take(12)) {
                            val isSelected = iconSelected.equals(iconName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { iconSelected = iconName },
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryIconBadge(
                                    iconName = iconName,
                                    colorHex = colorSelected,
                                    size = 30.dp,
                                    iconSize = 18.dp
                                )
                            }
                        }
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isBlank()) {
                            errorMsg = "Category name cannot be empty"
                        } else {
                            viewModel.saveCategory(nameText.trim(), iconSelected, colorSelected)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissAddEditDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddCategoryDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row (Expense / Income)
            TabRow(
                selectedTabIndex = if (uiState.selectedTab == TransactionType.EXPENSE) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.selectedTab == TransactionType.EXPENSE,
                    onClick = { viewModel.selectTab(TransactionType.EXPENSE) },
                    text = { Text("Expense Categories", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.selectedTab == TransactionType.INCOME,
                    onClick = { viewModel.selectTab(TransactionType.INCOME) },
                    text = { Text("Income Categories", fontWeight = FontWeight.SemiBold) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories, key = { it.id }) { cat ->
                    CategoryListItem(
                        category = cat,
                        onEdit = { viewModel.openEditCategoryDialog(cat) },
                        onDelete = { viewModel.requestDeleteCategory(cat) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryListItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(
                iconName = category.iconName,
                colorHex = category.colorHex,
                size = 40.dp,
                iconSize = 22.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (category.isDefault) {
                    Text(
                        text = "Default category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (!category.isDefault) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = ExpenseRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
