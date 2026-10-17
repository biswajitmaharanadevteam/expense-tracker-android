package com.expensemate.app

import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AccountType
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.export.CsvExporter
import com.expensemate.app.export.CsvImporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CsvExportImportTest {

    @Test
    fun testExportAndImportCycle() {
        val categories = listOf(
            Category(1, "Groceries", TransactionType.EXPENSE, "shopping_cart", "#10B981")
        )
        val accounts = listOf(
            Account(1, "Cash", AccountType.CASH, 0.0, 0.0, "#10B981")
        )

        val transactions = listOf(
            Transaction(
                id = 10,
                title = "Weekly Market, Veggies",
                amount = 450.75,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                categoryName = "Groceries",
                accountId = 1,
                accountName = "Cash",
                paymentMethod = PaymentMethod.CASH,
                date = 1700000000000L,
                payeeOrMerchant = "Local Vendor",
                notes = "Fresh vegetables",
                tags = listOf("food", "market")
            )
        )

        // Export to ByteArrayOutputStream
        val outputStream = ByteArrayOutputStream()
        CsvExporter.exportTransactions(transactions, outputStream)
        val csvString = outputStream.toString()

        // Verify CSV header and content
        assertTrue(csvString.contains("Weekly Market, Veggies"))
        assertTrue(csvString.contains("450.75"))

        // Import back using CsvImporter
        val inputStream = ByteArrayInputStream(csvString.toByteArray())
        val result = CsvImporter.importTransactions(inputStream, categories, accounts)

        assertEquals(1, result.importedTransactions.size)
        assertEquals(0, result.skippedRowCount)

        val imported = result.importedTransactions[0]
        assertEquals("Weekly Market, Veggies", imported.title)
        assertEquals(450.75, imported.amount, 0.001)
        assertEquals(TransactionType.EXPENSE, imported.type)
        assertEquals("Groceries", imported.categoryName)
        assertEquals(listOf("food", "market"), imported.tags)
    }

    @Test
    fun testImportValidationSkipsInvalidRows() {
        val categories = listOf(Category(1, "Food", TransactionType.EXPENSE, "restaurant", "#10B981"))
        val accounts = listOf(Account(1, "Cash", AccountType.CASH, 0.0, 0.0, "#10B981"))

        val invalidCsv = """
            ID,Title,Amount,Type,Category,Account,PaymentMethod,Date,Merchant,Notes,Tags
            1,Invalid Amount,-50.00,EXPENSE,Food,Cash,CASH,2026-10-17 10:00:00,Store,,
            2,Valid Entry,120.00,EXPENSE,Food,Cash,CASH,2026-10-17 10:00:00,Store,,
            3,Zero Amount,0.00,EXPENSE,Food,Cash,CASH,2026-10-17 10:00:00,Store,,
        """.trimIndent()

        val result = CsvImporter.importTransactions(ByteArrayInputStream(invalidCsv.toByteArray()), categories, accounts)

        assertEquals(1, result.importedTransactions.size)
        assertEquals(2, result.skippedRowCount) // Negative & Zero amount skipped
        assertEquals("Valid Entry", result.importedTransactions[0].title)
    }
}
