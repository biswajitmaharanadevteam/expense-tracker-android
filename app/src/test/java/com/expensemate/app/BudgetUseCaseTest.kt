package com.expensemate.app

import com.expensemate.app.domain.model.Budget
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.BudgetProgressUseCase
import com.expensemate.app.domain.usecase.BudgetStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetUseCaseTest {

    private val budgetProgressUseCase = BudgetProgressUseCase()

    @Test
    fun testBudgetCalculationsAndThresholds() {
        val budgets = listOf(
            Budget(id = 1, categoryId = 1, categoryName = "Dining", month = 10, year = 2026, amountLimit = 5000.0),
            Budget(id = 2, categoryId = 2, categoryName = "Groceries", month = 10, year = 2026, amountLimit = 10000.0)
        )

        val transactions = listOf(
            // Dining: spent 4200 (84% -> WARNING_80)
            Transaction(id = 1, title = "Meal 1", amount = 2200.0, type = TransactionType.EXPENSE, categoryId = 1, accountId = 1),
            Transaction(id = 2, title = "Meal 2", amount = 2000.0, type = TransactionType.EXPENSE, categoryId = 1, accountId = 1),

            // Groceries: spent 11000 (110% -> EXCEEDED_100)
            Transaction(id = 3, title = "Supermarket", amount = 11000.0, type = TransactionType.EXPENSE, categoryId = 2, accountId = 1),

            // Income: should NOT count towards spending!
            Transaction(id = 4, title = "Bonus", amount = 50000.0, type = TransactionType.INCOME, categoryId = 1, accountId = 1)
        )

        val results = budgetProgressUseCase(budgets, transactions)

        // Dining Budget
        val diningProgress = results.first { it.budget.id == 1L }
        assertEquals(4200.0, diningProgress.budget.amountSpent, 0.001)
        assertEquals(800.0, diningProgress.budget.remainingAmount, 0.001)
        assertEquals(84.0f, diningProgress.budget.percentageSpent, 0.1f)
        assertTrue(diningProgress.budget.isNearBudget)
        assertFalse(diningProgress.budget.isOverBudget)
        assertEquals(BudgetStatus.WARNING_80, diningProgress.status)

        // Groceries Budget
        val groceriesProgress = results.first { it.budget.id == 2L }
        assertEquals(11000.0, groceriesProgress.budget.amountSpent, 0.001)
        assertEquals(0.0, groceriesProgress.budget.remainingAmount, 0.001)
        assertTrue(groceriesProgress.budget.isOverBudget)
        assertEquals(BudgetStatus.EXCEEDED_100, groceriesProgress.status)
    }
}
