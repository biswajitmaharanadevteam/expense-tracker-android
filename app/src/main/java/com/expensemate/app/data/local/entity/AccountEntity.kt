package com.expensemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val initialBalance: Double = 0.0,
    val colorHex: String = "#2A3B90",
    val isDefault: Boolean = false
) {
    fun toDomain(currentBalance: Double = initialBalance): Account {
        return Account(
            id = id,
            name = name,
            type = try {
                AccountType.valueOf(type)
            } catch (e: Exception) {
                AccountType.CASH
            },
            initialBalance = initialBalance,
            currentBalance = currentBalance,
            colorHex = colorHex,
            isDefault = isDefault
        )
    }

    companion object {
        fun fromDomain(account: Account): AccountEntity {
            return AccountEntity(
                id = account.id,
                name = account.name,
                type = account.type.name,
                initialBalance = account.initialBalance,
                colorHex = account.colorHex,
                isDefault = account.isDefault
            )
        }
    }
}
