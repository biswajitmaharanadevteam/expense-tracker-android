package com.expensemate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensemate.app.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

data class AccountBalanceResult(
    val accountId: Long,
    val name: String,
    val type: String,
    val initialBalance: Double,
    val colorHex: String,
    val isDefault: Boolean,
    val totalIncome: Double,
    val totalExpense: Double,
    val currentBalance: Double
)

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("""
        SELECT a.id AS accountId, a.name, a.type, a.initialBalance, a.colorHex, a.isDefault,
               COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'INCOME'), 0.0) AS totalIncome,
               COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'EXPENSE'), 0.0) AS totalExpense,
               (a.initialBalance 
                + COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'INCOME'), 0.0) 
                - COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'EXPENSE'), 0.0)
               ) AS currentBalance
        FROM accounts a
        ORDER BY a.isDefault DESC, a.name ASC
    """)
    fun getAccountsWithComputedBalancesFlow(): Flow<List<AccountBalanceResult>>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getCount(): Int

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}
