package com.expensemate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensemate.app.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

data class RecurringRuleWithDetails(
    val id: Long,
    val title: String,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val categoryName: String?,
    val accountId: Long,
    val accountName: String?,
    val paymentMethod: String,
    val frequency: String,
    val startDate: Long,
    val endDate: Long?,
    val lastExecutedDate: Long?,
    val isActive: Boolean,
    val reminderEnabled: Boolean
)

@Dao
interface RecurringRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RecurringRuleEntity): Long

    @Update
    suspend fun updateRule(rule: RecurringRuleEntity)

    @Delete
    suspend fun deleteRule(rule: RecurringRuleEntity)

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM recurring_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Long): RecurringRuleEntity?

    @Query("""
        SELECT r.id, r.title, r.amount, r.type, r.categoryId, c.name AS categoryName,
               r.accountId, a.name AS accountName, r.paymentMethod, r.frequency,
               r.startDate, r.endDate, r.lastExecutedDate, r.isActive, r.reminderEnabled
        FROM recurring_rules r
        LEFT JOIN categories c ON r.categoryId = c.id
        LEFT JOIN accounts a ON r.accountId = a.id
        ORDER BY r.id DESC
    """)
    fun getAllRulesWithDetailsFlow(): Flow<List<RecurringRuleWithDetails>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1")
    suspend fun getActiveRules(): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules")
    suspend fun getAllRules(): List<RecurringRuleEntity>

    @Query("DELETE FROM recurring_rules")
    suspend fun deleteAll()
}
