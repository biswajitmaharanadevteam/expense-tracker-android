package com.expensemate.app.export

import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

data class ImportResult(
    val importedTransactions: List<Transaction>,
    val skippedRowCount: Int,
    val errors: List<String>
)

object CsvImporter {

    fun importTransactions(
        inputStream: InputStream,
        existingCategories: List<Category>,
        existingAccounts: List<Account>
    ): ImportResult {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val importedList = mutableListOf<Transaction>()
        val errors = mutableListOf<String>()
        var skippedCount = 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fallbackDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val defaultCategory = existingCategories.firstOrNull()
        val defaultAccount = existingAccounts.firstOrNull()

        if (defaultCategory == null || defaultAccount == null) {
            return ImportResult(
                importedTransactions = emptyList(),
                skippedRowCount = 0,
                errors = listOf("Cannot import: no default categories or accounts configured.")
            )
        }

        var lineIndex = 0
        reader.useLines { lines ->
            for (rawLine in lines) {
                lineIndex++
                val line = rawLine.trim()
                if (line.isBlank()) continue

                // Skip header line
                if (lineIndex == 1 && line.uppercase().contains("AMOUNT") && line.uppercase().contains("TITLE")) {
                    continue
                }

                val tokens = parseCsvLine(line)
                if (tokens.size < 4) {
                    errors.add("Line $lineIndex: Insufficient columns (${tokens.size} found, minimum 4 required).")
                    skippedCount++
                    continue
                }

                try {
                    // Supported formats:
                    // ID,Title,Amount,Type,Category,Account,PaymentMethod,Date,Merchant,Notes,Tags
                    val title = tokens.getOrNull(1)?.trim().takeUnless { it.isNullOrBlank() } ?: "Imported Transaction"
                    val amountRaw = tokens.getOrNull(2)?.trim()?.replace("$", "")?.replace("₹", "") ?: "0"
                    val amount = amountRaw.toDoubleOrNull()

                    if (amount == null || amount <= 0.0) {
                        errors.add("Line $lineIndex: Invalid transaction amount '$amountRaw'. Must be positive number.")
                        skippedCount++
                        continue
                    }

                    val typeStr = tokens.getOrNull(3)?.trim()?.uppercase()
                    val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE

                    val categoryName = tokens.getOrNull(4)?.trim() ?: ""
                    val matchedCategory = existingCategories.firstOrNull {
                        it.name.equals(categoryName, ignoreCase = true) && it.type == type
                    } ?: existingCategories.firstOrNull { it.type == type } ?: defaultCategory

                    val accountName = tokens.getOrNull(5)?.trim() ?: ""
                    val matchedAccount = existingAccounts.firstOrNull {
                        it.name.equals(accountName, ignoreCase = true)
                    } ?: defaultAccount

                    val paymentMethodStr = tokens.getOrNull(6)?.trim()?.uppercase() ?: ""
                    val paymentMethod = try {
                        PaymentMethod.valueOf(paymentMethodStr)
                    } catch (e: Exception) {
                        PaymentMethod.CASH
                    }

                    val dateStr = tokens.getOrNull(7)?.trim() ?: ""
                    val date = try {
                        dateFormat.parse(dateStr)?.time
                            ?: fallbackDateFormat.parse(dateStr)?.time
                            ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    val merchant = tokens.getOrNull(8)?.trim() ?: ""
                    val notes = tokens.getOrNull(9)?.trim() ?: ""
                    val tagsStr = tokens.getOrNull(10)?.trim() ?: ""
                    val tags = if (tagsStr.isBlank()) emptyList() else tagsStr.split(";").map { it.trim() }

                    val transaction = Transaction(
                        id = 0, // Auto-generate new IDs during import to prevent collision
                        title = title,
                        amount = amount,
                        type = type,
                        categoryId = matchedCategory.id,
                        categoryName = matchedCategory.name,
                        categoryIcon = matchedCategory.iconName,
                        categoryColor = matchedCategory.colorHex,
                        accountId = matchedAccount.id,
                        accountName = matchedAccount.name,
                        paymentMethod = paymentMethod,
                        date = date,
                        payeeOrMerchant = merchant,
                        notes = notes,
                        tags = tags
                    )

                    importedList.add(transaction)
                } catch (e: Exception) {
                    errors.add("Line $lineIndex: Failed to parse line (${e.message})")
                    skippedCount++
                }
            }
        }

        return ImportResult(
            importedTransactions = importedList,
            skippedRowCount = skippedCount,
            errors = errors
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++ // skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        result.add(sb.toString())
        return result
    }
}
