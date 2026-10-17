package com.expensemate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensemate.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

data class BudgetWithCategory(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val month: Int,
    val year: Int,
    val amountLimit: Double
)

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>): List<Long>

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Query("""
        SELECT b.id, b.categoryId, c.name AS categoryName, c.iconName AS categoryIcon, c.colorHex AS categoryColor,
               b.month, b.year, b.amountLimit
        FROM budgets b
        LEFT JOIN categories c ON b.categoryId = c.id
        WHERE b.month = :month AND b.year = :year
        ORDER BY b.categoryId IS NOT NULL, b.id ASC
    """)
    fun getBudgetsForMonthFlow(month: Int, year: Int): Flow<List<BudgetWithCategory>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetForCategoryAndMonth(categoryId: Long, month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND month = :month AND year = :year LIMIT 1")
    suspend fun getOverallBudgetForMonth(month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<BudgetEntity>

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}
