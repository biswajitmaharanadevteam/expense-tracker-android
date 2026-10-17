package com.expensemate.app.domain.usecase

import com.expensemate.app.domain.model.RecurrenceFrequency
import com.expensemate.app.domain.model.RecurringRule
import com.expensemate.app.domain.model.Transaction
import java.util.Calendar

data class ProcessRecurringResult(
    val newTransactions: List<Transaction>,
    val updatedRules: List<RecurringRule>
)

class ProcessRecurringTransactionsUseCase {

    operator fun invoke(
        rules: List<RecurringRule>,
        currentTimestamp: Long = System.currentTimeMillis()
    ): ProcessRecurringResult {
        val newTransactions = mutableListOf<Transaction>()
        val updatedRules = mutableListOf<RecurringRule>()

        for (rule in rules) {
            if (!rule.isActive) continue

            // Check if end date passed
            if (rule.endDate != null && currentTimestamp > rule.endDate) {
                updatedRules.add(rule.copy(isActive = false))
                continue
            }

            // Determine if rule is due
            val lastExecution = rule.lastExecutedDate ?: (rule.startDate - 1000)
            val isDue = isRuleDue(lastExecution, currentTimestamp, rule.frequency)

            if (isDue && currentTimestamp >= rule.startDate) {
                val transaction = Transaction(
                    title = rule.title,
                    amount = rule.amount,
                    type = rule.type,
                    categoryId = rule.categoryId,
                    categoryName = rule.categoryName,
                    accountId = rule.accountId,
                    accountName = rule.accountName,
                    paymentMethod = rule.paymentMethod,
                    date = currentTimestamp,
                    payeeOrMerchant = "Recurring: ${rule.title}",
                    notes = "Automatically recorded recurring ${rule.frequency.displayName.lowercase()}",
                    isRecurring = true,
                    recurringRuleId = rule.id
                )
                newTransactions.add(transaction)
                updatedRules.add(rule.copy(lastExecutedDate = currentTimestamp))
            }
        }

        return ProcessRecurringResult(newTransactions, updatedRules)
    }

    private fun isRuleDue(lastExecuted: Long, current: Long, frequency: RecurrenceFrequency): Boolean {
        val calLast = Calendar.getInstance().apply { timeInMillis = lastExecuted }
        val calCurrent = Calendar.getInstance().apply { timeInMillis = current }

        return when (frequency) {
            RecurrenceFrequency.DAILY -> {
                calCurrent.get(Calendar.DAY_OF_YEAR) != calLast.get(Calendar.DAY_OF_YEAR) ||
                        calCurrent.get(Calendar.YEAR) != calLast.get(Calendar.YEAR)
            }
            RecurrenceFrequency.WEEKLY -> {
                calCurrent.get(Calendar.WEEK_OF_YEAR) != calLast.get(Calendar.WEEK_OF_YEAR) ||
                        calCurrent.get(Calendar.YEAR) != calLast.get(Calendar.YEAR)
            }
            RecurrenceFrequency.MONTHLY -> {
                calCurrent.get(Calendar.MONTH) != calLast.get(Calendar.MONTH) ||
                        calCurrent.get(Calendar.YEAR) != calLast.get(Calendar.YEAR)
            }
            RecurrenceFrequency.YEARLY -> {
                calCurrent.get(Calendar.YEAR) != calLast.get(Calendar.YEAR)
            }
        }
    }
}
