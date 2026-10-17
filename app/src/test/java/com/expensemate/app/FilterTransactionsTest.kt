package com.expensemate.app

import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.FilterTransactionsUseCase
import com.expensemate.app.domain.usecase.TransactionFilterCriteria
import com.expensemate.app.domain.usecase.TransactionSortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterTransactionsTest {

    private val filterUseCase = FilterTransactionsUseCase()

    private val sampleTransactions = listOf(
        Transaction(id = 1, title = "Starbucks Coffee", amount = 350.0, type = TransactionType.EXPENSE, categoryId = 1, categoryName = "Food", accountId = 1, paymentMethod = PaymentMethod.UPI, date = 1000L),
        Transaction(id = 2, title = "Supermarket Grocery", amount = 1200.0, type = TransactionType.EXPENSE, categoryId = 2, categoryName = "Groceries", accountId = 1, paymentMethod = PaymentMethod.CASH, date = 2000L),
        Transaction(id = 3, title = "Freelance Project", amount = 25000.0, type = TransactionType.INCOME, categoryId = 102, categoryName = "Freelancing", accountId = 2, paymentMethod = PaymentMethod.BANK_ACCOUNT, date = 3000L)
    )

    @Test
    fun testTextSearchFilter() {
        val criteria = TransactionFilterCriteria(query = "coffee")
        val result = filterUseCase(sampleTransactions, criteria)
        assertEquals(1, result.size)
        assertEquals("Starbucks Coffee", result[0].title)
    }

    @Test
    fun testTypeAndCategoryFilter() {
        val criteria = TransactionFilterCriteria(type = TransactionType.EXPENSE, categoryId = 2)
        val result = filterUseCase(sampleTransactions, criteria)
        assertEquals(1, result.size)
        assertEquals("Supermarket Grocery", result[0].title)
    }

    @Test
    fun testSortByAmountDescending() {
        val criteria = TransactionFilterCriteria(sortOption = TransactionSortOption.AMOUNT_DESC)
        val result = filterUseCase(sampleTransactions, criteria)
        assertEquals(25000.0, result[0].amount, 0.001)
        assertEquals(1200.0, result[1].amount, 0.001)
        assertEquals(350.0, result[2].amount, 0.001)
    }
}
