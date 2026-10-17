package com.expensemate.app.util

import com.expensemate.app.domain.model.DateFormatOption
import com.expensemate.app.domain.model.DateRange
import com.expensemate.app.domain.model.FirstDayOfWeek
import com.expensemate.app.domain.model.PeriodType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatDate(timestamp: Long, formatOption: DateFormatOption = DateFormatOption.DD_MM_YYYY): String {
        val sdf = SimpleDateFormat(formatOption.pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(month: Int, year: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun formatShortMonth(month: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun formatRelativeDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.timeInMillis = timestamp
        val txYear = calendar.get(Calendar.YEAR)
        val txDay = calendar.get(Calendar.DAY_OF_YEAR)

        return when {
            todayYear == txYear && todayDay == txDay -> "Today"
            todayYear == txYear && todayDay - txDay == 1 -> "Yesterday"
            else -> {
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun getDateRangeForPeriod(
        periodType: PeriodType,
        firstDayOfWeek: FirstDayOfWeek = FirstDayOfWeek.MONDAY,
        customStart: Long? = null,
        customEnd: Long? = null
    ): DateRange {
        val calendar = Calendar.getInstance()

        return when (periodType) {
            PeriodType.THIS_WEEK -> {
                calendar.firstDayOfWeek = firstDayOfWeek.calendarDay
                calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek.calendarDay)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis

                DateRange(start, end, "This Week")
            }

            PeriodType.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis

                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(start))
                DateRange(start, end, monthName)
            }

            PeriodType.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis

                DateRange(start, end, calendar.get(Calendar.YEAR).toString())
            }

            PeriodType.CUSTOM -> {
                val start = customStart ?: (System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
                val end = customEnd ?: System.currentTimeMillis()
                DateRange(start, end, "Custom Range")
            }
        }
    }

    fun getMonthStartAndEnd(month: Int, year: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
