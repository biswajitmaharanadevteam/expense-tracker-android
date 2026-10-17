package com.expensemate.app.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.FinancialSummary
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.util.CurrencyFormatter
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportExporter {

    fun generatePdfReport(
        summary: FinancialSummary,
        transactions: List<Transaction>,
        periodTitle: String,
        currency: CurrencyConfig,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Header Background
        paint.color = Color.rgb(42, 59, 144) // #2A3B90 Indigo
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("ExpenseMate - Financial Report", 40f, 45f, paint)

        // Subtitle / Period
        paint.textSize = 12f
        paint.isFakeBoldText = false
        val nowFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Period: $periodTitle  |  Generated: $nowFormatted", 40f, 70f, paint)

        // Financial Overview Cards (White / Light Grey boxes)
        var y = 120f
        paint.color = Color.rgb(241, 245, 249) // Slate 100
        canvas.drawRoundRect(40f, y, 280f, y + 60f, 8f, 8f, paint)
        canvas.drawRoundRect(315f, y, 555f, y + 60f, 8f, 8f, paint)

        paint.color = Color.rgb(100, 116, 139) // Slate 500
        paint.textSize = 10f
        canvas.drawText("TOTAL INCOME", 55f, y + 22f, paint)
        canvas.drawText("TOTAL EXPENSES", 330f, y + 22f, paint)

        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.color = Color.rgb(16, 185, 129) // Emerald Green
        canvas.drawText(CurrencyFormatter.format(summary.periodIncome, currency), 55f, y + 48f, paint)

        paint.color = Color.rgb(239, 68, 68) // Red
        canvas.drawText(CurrencyFormatter.format(summary.periodExpense, currency), 330f, y + 48f, paint)

        y += 75f
        // Row 2: Net Cash Flow & Total Balance
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(40f, y, 280f, y + 60f, 8f, 8f, paint)
        canvas.drawRoundRect(315f, y, 555f, y + 60f, 8f, 8f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("NET CASH FLOW", 55f, y + 22f, paint)
        canvas.drawText("ACCOUNT BALANCE", 330f, y + 22f, paint)

        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.color = if (summary.netCashFlow >= 0) Color.rgb(16, 185, 129) else Color.rgb(239, 68, 68)
        canvas.drawText(CurrencyFormatter.format(summary.netCashFlow, currency), 55f, y + 48f, paint)

        paint.color = Color.rgb(42, 59, 144)
        canvas.drawText(CurrencyFormatter.format(summary.totalBalance, currency), 330f, y + 48f, paint)

        // Section: Recent Transactions Header
        y += 90f
        paint.color = Color.rgb(15, 23, 42) // Dark Slate
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Transaction Summary (${transactions.size} records)", 40f, y, paint)

        // Table Header
        y += 20f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(40f, y, 555f, y + 24f, paint)

        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Date", 50f, y + 16f, paint)
        canvas.drawText("Title / Payee", 130f, y + 16f, paint)
        canvas.drawText("Category", 300f, y + 16f, paint)
        canvas.drawText("Type", 410f, y + 16f, paint)
        canvas.drawText("Amount", 480f, y + 16f, paint)

        y += 24f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        val maxRecords = transactions.take(20) // Fit cleanly in first page
        for (tx in maxRecords) {
            y += 20f
            if (y > 800f) break

            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText(dateFormat.format(Date(tx.date)), 50f, y, paint)

            val displayTitle = if (tx.title.length > 22) tx.title.substring(0, 20) + ".." else tx.title
            canvas.drawText(displayTitle, 130f, y, paint)

            val displayCat = if (tx.categoryName.length > 16) tx.categoryName.substring(0, 14) + ".." else tx.categoryName
            canvas.drawText(displayCat, 300f, y, paint)

            canvas.drawText(tx.type.name, 410f, y, paint)

            paint.color = if (tx.type.name == "INCOME") Color.rgb(16, 185, 129) else Color.rgb(239, 68, 68)
            paint.isFakeBoldText = true
            val amountStr = CurrencyFormatter.format(tx.amount, currency)
            canvas.drawText(amountStr, 480f, y, paint)
            paint.isFakeBoldText = false
        }

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 9f
        canvas.drawText("Generated by ExpenseMate - Secure Offline Android Expense Tracker", 140f, 825f, paint)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
