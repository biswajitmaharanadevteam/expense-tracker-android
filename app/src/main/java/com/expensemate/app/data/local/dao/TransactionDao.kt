package com.expensemate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensemate.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class TransactionWithDetails(
    val id: Long,
    val title: String,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val accountId: Long,
    val accountName: String,
    val paymentMethod: String,
    val date: Long,
    val payeeOrMerchant: String,
    val notes: String,
    val tags: String,
    val isRecurring: Boolean,
    val recurringRuleId: Long?
)

data class CategorySum(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val iconName: String,
    val totalAmount: Double
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("""
        SELECT t.id, t.title, t.amount, t.type, t.categoryId,
               COALESCE(c.name, 'Unknown') AS categoryName,
               COALESCE(c.iconName, 'category') AS categoryIcon,
               COALESCE(c.colorHex, '#2A3B90') AS categoryColor,
               t.accountId,
               COALESCE(a.name, 'Default Account') AS accountName,
               t.paymentMethod, t.date, t.payeeOrMerchant, t.notes, t.tags,
               t.isRecurring, t.recurringRuleId
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts a ON t.accountId = a.id
        ORDER BY t.date DESC
    """)
    fun getAllTransactionsWithDetailsFlow(): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT t.id, t.title, t.amount, t.type, t.categoryId,
               COALESCE(c.name, 'Unknown') AS categoryName,
               COALESCE(c.iconName, 'category') AS categoryIcon,
               COALESCE(c.colorHex, '#2A3B90') AS categoryColor,
               t.accountId,
               COALESCE(a.name, 'Default Account') AS accountName,
               t.paymentMethod, t.date, t.payeeOrMerchant, t.notes, t.tags,
               t.isRecurring, t.recurringRuleId
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts a ON t.accountId = a.id
        WHERE t.date BETWEEN :startDate AND :endDate
        ORDER BY t.date DESC
    """)
    fun getTransactionsWithDetailsBetweenDatesFlow(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT t.id, t.title, t.amount, t.type, t.categoryId,
               COALESCE(c.name, 'Unknown') AS categoryName,
               COALESCE(c.iconName, 'category') AS categoryIcon,
               COALESCE(c.colorHex, '#2A3B90') AS categoryColor,
               t.accountId,
               COALESCE(a.name, 'Default Account') AS accountName,
               t.paymentMethod, t.date, t.payeeOrMerchant, t.notes, t.tags,
               t.isRecurring, t.recurringRuleId
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts a ON t.accountId = a.id
        WHERE (t.title LIKE '%' || :query || '%' 
               OR t.payeeOrMerchant LIKE '%' || :query || '%' 
               OR t.notes LIKE '%' || :query || '%' 
               OR c.name LIKE '%' || :query || '%')
        ORDER BY t.date DESC
    """)
    fun searchTransactionsFlow(query: String): Flow<List<TransactionWithDetails>>

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getCountByCategoryId(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun getCountByAccountId(accountId: Long): Int

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpenseBetweenDatesFlow(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    fun getTotalIncomeBetweenDatesFlow(startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT t.categoryId, c.name AS categoryName, c.colorHex, c.iconName, SUM(t.amount) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate
        GROUP BY t.categoryId, c.name, c.colorHex, c.iconName
        ORDER BY totalAmount DESC
    """)
    fun getCategoryExpenseSumsBetweenDatesFlow(startDate: Long, endDate: Long): Flow<List<CategorySum>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM transactions 
        WHERE type = 'EXPENSE' 
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getSpentAmountForBudget(categoryId: Long?, startDate: Long, endDate: Long): Double

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions")
    suspend fun getAllRawTransactions(): List<TransactionEntity>
}
