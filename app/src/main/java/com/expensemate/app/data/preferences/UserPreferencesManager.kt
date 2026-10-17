package com.expensemate.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.DateFormatOption
import com.expensemate.app.domain.model.FirstDayOfWeek
import com.expensemate.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val CURRENCY_NAME = stringPreferencesKey("currency_name")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val DEFAULT_EXPENSE_CAT = longPreferencesKey("default_expense_cat")
        val DEFAULT_INCOME_CAT = longPreferencesKey("default_income_cat")
        val DEFAULT_ACCOUNT_ID = longPreferencesKey("default_account_id")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val BUDGET_ALERTS_ENABLED = booleanPreferencesKey("budget_alerts_enabled")
        val RECURRING_ALERTS_ENABLED = booleanPreferencesKey("recurring_alerts_enabled")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }

        val currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: CurrencyConfig.DEFAULT.code
        val currencySymbol = preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: CurrencyConfig.DEFAULT.symbol
        val currencyName = preferences[PreferencesKeys.CURRENCY_NAME] ?: CurrencyConfig.DEFAULT.name
        val currency = CurrencyConfig(currencyCode, currencySymbol, currencyName)

        val firstDayStr = preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: FirstDayOfWeek.MONDAY.name
        val firstDayOfWeek = try {
            FirstDayOfWeek.valueOf(firstDayStr)
        } catch (e: Exception) {
            FirstDayOfWeek.MONDAY
        }

        val dateFormatStr = preferences[PreferencesKeys.DATE_FORMAT] ?: DateFormatOption.DD_MM_YYYY.name
        val dateFormat = try {
            DateFormatOption.valueOf(dateFormatStr)
        } catch (e: Exception) {
            DateFormatOption.DD_MM_YYYY
        }

        AppSettings(
            themeMode = themeMode,
            currency = currency,
            firstDayOfWeek = firstDayOfWeek,
            dateFormat = dateFormat,
            defaultExpenseCategoryId = preferences[PreferencesKeys.DEFAULT_EXPENSE_CAT] ?: 1L,
            defaultIncomeCategoryId = preferences[PreferencesKeys.DEFAULT_INCOME_CAT] ?: 101L,
            defaultAccountId = preferences[PreferencesKeys.DEFAULT_ACCOUNT_ID] ?: 1L,
            dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: true,
            dailyReminderHour = preferences[PreferencesKeys.DAILY_REMINDER_HOUR] ?: 20,
            dailyReminderMinute = preferences[PreferencesKeys.DAILY_REMINDER_MINUTE] ?: 0,
            budgetAlertsEnabled = preferences[PreferencesKeys.BUDGET_ALERTS_ENABLED] ?: true,
            recurringAlertsEnabled = preferences[PreferencesKeys.RECURRING_ALERTS_ENABLED] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setCurrency(currency: CurrencyConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] = currency.code
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = currency.symbol
            preferences[PreferencesKeys.CURRENCY_NAME] = currency.name
        }
    }

    suspend fun setFirstDayOfWeek(firstDay: FirstDayOfWeek) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] = firstDay.name
        }
    }

    suspend fun setDateFormat(format: DateFormatOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT] = format.name
        }
    }

    suspend fun setDailyReminder(enabled: Boolean, hour: Int = 20, minute: Int = 0) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.DAILY_REMINDER_HOUR] = hour
            preferences[PreferencesKeys.DAILY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDGET_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setRecurringAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECURRING_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
