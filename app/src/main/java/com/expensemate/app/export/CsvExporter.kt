package com.expensemate.app.export

import com.expensemate.app.domain.model.Transaction
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private const val CSV_HEADER = "ID,Title,Amount,Type,Category,Account,PaymentMethod,Date,Merchant,Notes,Tags"

    fun exportTransactions(transactions: List<Transaction>, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        writer.write(CSV_HEADER)
        writer.write("\n")

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.date))
            val tagsStr = tx.tags.joinToString(";")
            val line = listOf(
                tx.id.toString(),
                escapeCsv(tx.title),
                String.format(Locale.US, "%.2f", tx.amount),
                tx.type.name,
                escapeCsv(tx.categoryName),
                escapeCsv(tx.accountName),
                tx.paymentMethod.name,
                dateStr,
                escapeCsv(tx.payeeOrMerchant),
                escapeCsv(tx.notes),
                escapeCsv(tagsStr)
            ).joinToString(",")

            writer.write(line)
            writer.write("\n")
        }

        writer.flush()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
