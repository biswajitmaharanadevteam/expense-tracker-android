package com.expensemate.app

import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.RecurrenceFrequency
import com.expensemate.app.domain.model.RecurringRule
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.ProcessRecurringTransactionsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RecurringTransactionTest {

    private val useCase = ProcessRecurringTransactionsUseCase()

    @Test
    fun testRecurringTransactionExecutionAndDeduplication() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Rule started yesterday, never executed
        val yesterday = now - 24 * 3600 * 1000L
        val rule = RecurringRule(
            id = 1,
            title = "Gym Membership",
            amount = 1500.0,
            type = TransactionType.EXPENSE,
            categoryId = 14,
            accountId = 1,
            frequency = RecurrenceFrequency.DAILY,
            startDate = yesterday,
            lastExecutedDate = yesterday,
            isActive = true
        )

        // 1. Process today -> should generate transaction
        val result1 = useCase(listOf(rule), currentTimestamp = now)
        assertEquals(1, result1.newTransactions.size)
        assertEquals("Gym Membership", result1.newTransactions[0].title)
        assertEquals(1500.0, result1.newTransactions[0].amount, 0.001)

        val updatedRule = result1.updatedRules[0]
        assertEquals(now, updatedRule.lastExecutedDate)

        // 2. Process again on the SAME day -> should NOT generate duplicate!
        val result2 = useCase(listOf(updatedRule), currentTimestamp = now + 1000)
        assertEquals(0, result2.newTransactions.size)
    }

    @Test
    fun testExpiredRuleBecomesInactive() {
        val now = System.currentTimeMillis()
        val expiredRule = RecurringRule(
            id = 2,
            title = "Fixed Term Loan",
            amount = 5000.0,
            type = TransactionType.EXPENSE,
            categoryId = 15,
            accountId = 1,
            frequency = RecurrenceFrequency.MONTHLY,
            startDate = now - 60 * 86400000L,
            endDate = now - 1000L, // ended 1 second ago
            isActive = true
        )

        val result = useCase(listOf(expiredRule), currentTimestamp = now)
        assertEquals(0, result.newTransactions.size)
        assertEquals(1, result.updatedRules.size)
        assertEquals(false, result.updatedRules[0].isActive)
    }
}
