package com.expensemate.app.domain.model

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class CurrencyConfig(
    val code: String,
    val symbol: String,
    val name: String
) {
    companion object {
        val DEFAULT = CurrencyConfig("INR", "₹", "Indian Rupee")

        val SUPPORTED_CURRENCIES = listOf(
            CurrencyConfig("INR", "₹", "Indian Rupee (INR)"),
            CurrencyConfig("USD", "$", "US Dollar (USD)"),
            CurrencyConfig("EUR", "€", "Euro (EUR)"),
            CurrencyConfig("GBP", "£", "British Pound (GBP)"),
            CurrencyConfig("JPY", "¥", "Japanese Yen (JPY)"),
            CurrencyConfig("CAD", "CA$", "Canadian Dollar (CAD)"),
            CurrencyConfig("AUD", "A$", "Australian Dollar (AUD)"),
            CurrencyConfig("SGD", "S$", "Singapore Dollar (SGD)"),
            CurrencyConfig("AED", "AED", "UAE Dirham (AED)")
        )
    }
}

enum class FirstDayOfWeek(val displayName: String, val calendarDay: Int) {
    MONDAY("Monday", java.util.Calendar.MONDAY),
    SUNDAY("Sunday", java.util.Calendar.SUNDAY)
}

enum class DateFormatOption(val pattern: String, val preview: String) {
    DD_MM_YYYY("dd/MM/yyyy", "17/10/2026"),
    MM_DD_YYYY("MM/dd/yyyy", "10/17/2026"),
    YYYY_MM_DD("yyyy-MM-dd", "2026-10-17")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: CurrencyConfig = CurrencyConfig.DEFAULT,
    val firstDayOfWeek: FirstDayOfWeek = FirstDayOfWeek.MONDAY,
    val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    val defaultExpenseCategoryId: Long = 1,
    val defaultIncomeCategoryId: Long = 101,
    val defaultAccountId: Long = 1,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0,
    val budgetAlertsEnabled: Boolean = true,
    val recurringAlertsEnabled: Boolean = true
)
