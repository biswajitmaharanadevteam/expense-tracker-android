package com.expensemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "EXPENSE" or "INCOME"
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            type = if (type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE,
            iconName = iconName,
            colorHex = colorHex,
            isDefault = isDefault
        )
    }

    companion object {
        fun fromDomain(category: Category): CategoryEntity {
            return CategoryEntity(
                id = category.id,
                name = category.name,
                type = category.type.name,
                iconName = category.iconName,
                colorHex = category.colorHex,
                isDefault = category.isDefault
            )
        }
    }
}
