package com.expensemate.app.domain.usecase

import com.expensemate.app.domain.model.Budget
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType

enum class BudgetStatus {
    NORMAL,
    WARNING_80,
    EXCEEDED_100
}

data class BudgetProgress(
    val budget: Budget,
    val status: BudgetStatus
)

class BudgetProgressUseCase {

    operator fun invoke(budgets: List<Budget>, transactions: List<Transaction>): List<BudgetProgress> {
        val expenseTx = transactions.filter { it.type == TransactionType.EXPENSE }

        return budgets.map { budget ->
            val spent = expenseTx
                .filter { budget.categoryId == null || it.categoryId == budget.categoryId }
                .sumOf { it.amount }

            val updatedBudget = budget.copy(amountSpent = spent)
            val status = when {
                updatedBudget.isOverBudget -> BudgetStatus.EXCEEDED_100
                updatedBudget.isNearBudget -> BudgetStatus.WARNING_80
                else -> BudgetStatus.NORMAL
            }

            BudgetProgress(budget = updatedBudget, status = status)
        }
    }
}
