package com.expensemate.app.util

import com.expensemate.app.domain.model.CurrencyConfig
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    fun format(amount: Double, currency: CurrencyConfig = CurrencyConfig.DEFAULT): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formattedNumber = formatter.format(Math.abs(amount))
        return if (amount < 0) {
            "-${currency.symbol}$formattedNumber"
        } else {
            "${currency.symbol}$formattedNumber"
        }
    }

    fun formatCompact(amount: Double, currency: CurrencyConfig = CurrencyConfig.DEFAULT): String {
        val abs = Math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            abs >= 10_000_000 -> "$sign${currency.symbol}${DecimalFormat("#0.0").format(abs / 10_000_000)}Cr"
            abs >= 100_000 -> "$sign${currency.symbol}${DecimalFormat("#0.0").format(abs / 100_000)}L"
            abs >= 1_000 -> "$sign${currency.symbol}${DecimalFormat("#0.0").format(abs / 1_000)}k"
            else -> "$sign${currency.symbol}${DecimalFormat("#0").format(abs)}"
        }
    }

    fun parseAmount(input: String): Double? {
        val clean = input.trim().replace(",", ".")
        return clean.toDoubleOrNull()?.let {
            if (it >= 0) it else null
        }
    }
}
