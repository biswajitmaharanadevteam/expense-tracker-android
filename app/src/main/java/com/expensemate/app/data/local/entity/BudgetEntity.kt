package com.expensemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensemate.app.domain.model.Budget

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId", "month", "year"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long?, // null for total overall monthly budget
    val month: Int,
    val year: Int,
    val amountLimit: Double
) {
    fun toDomain(
        categoryName: String = "Overall Budget",
        categoryIcon: String = "account_balance_wallet",
        categoryColor: String = "#2A3B90",
        amountSpent: Double = 0.0
    ): Budget {
        return Budget(
            id = id,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryIcon = categoryIcon,
            categoryColor = categoryColor,
            month = month,
            year = year,
            amountLimit = amountLimit,
            amountSpent = amountSpent
        )
    }

    companion object {
        fun fromDomain(budget: Budget): BudgetEntity {
            return BudgetEntity(
                id = budget.id,
                categoryId = budget.categoryId,
                month = budget.month,
                year = budget.year,
                amountLimit = budget.amountLimit
            )
        }
    }
}
