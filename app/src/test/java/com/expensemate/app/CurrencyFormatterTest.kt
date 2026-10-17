package com.expensemate.app

import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun testCurrencyFormatting() {
        val inr = CurrencyConfig("INR", "₹", "Indian Rupee")
        val usd = CurrencyConfig("USD", "$", "US Dollar")

        assertEquals("₹1,250.50", CurrencyFormatter.format(1250.50, inr))
        assertEquals("$500.00", CurrencyFormatter.format(500.0, usd))
        assertEquals("-₹350.00", CurrencyFormatter.format(-350.0, inr))
    }

    @Test
    fun testCompactFormatting() {
        val inr = CurrencyConfig("INR", "₹", "Indian Rupee")

        assertEquals("₹15.0k", CurrencyFormatter.formatCompact(15000.0, inr))
        assertEquals("₹2.5L", CurrencyFormatter.formatCompact(250000.0, inr))
        assertEquals("₹1.2Cr", CurrencyFormatter.formatCompact(12000000.0, inr))
    }

    @Test
    fun testAmountParsing() {
        assertEquals(150.75, CurrencyFormatter.parseAmount("150.75"))
        assertEquals(150.75, CurrencyFormatter.parseAmount("150,75")) // Handles comma as decimal
        assertEquals(1000.0, CurrencyFormatter.parseAmount(" 1000 "))
        assertNull(CurrencyFormatter.parseAmount("-50.0")) // Rejects negative
        assertNull(CurrencyFormatter.parseAmount("abc"))
    }
}
