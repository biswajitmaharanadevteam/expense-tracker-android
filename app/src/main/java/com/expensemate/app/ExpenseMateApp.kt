package com.expensemate.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.expensemate.app.data.local.ExpenseMateDatabase
import com.expensemate.app.data.preferences.UserPreferencesManager
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.data.repository.ExpenseMateRepositoryImpl
import com.expensemate.app.notifications.NotificationHelper
import com.expensemate.app.worker.DailyReminderWorker
import com.expensemate.app.worker.RecurringTransactionWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ExpenseMateApp : Application() {

    lateinit var repository: ExpenseMateRepository
        private set

    lateinit var preferencesManager: UserPreferencesManager
        private set

    lateinit var database: ExpenseMateDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Database & Preferences
        database = ExpenseMateDatabase.getInstance(this)
        preferencesManager = UserPreferencesManager(this)
        repository = ExpenseMateRepositoryImpl(database, preferencesManager)

        // 2. Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // 3. Setup WorkManager Background Tasks
        scheduleBackgroundWorkers()
    }

    private fun scheduleBackgroundWorkers() {
        val workManager = WorkManager.getInstance(this)

        // Recurring Transaction Execution Worker (runs every 6 hours)
        val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            6, TimeUnit.HOURS,
            30, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "recurring_transactions_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringWorkRequest
        )

        // Daily Reminder Worker (calculated delay to target 8:00 PM every day)
        val calendar = Calendar.getInstance()
        val nowMillis = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 20)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = calendar.timeInMillis - nowMillis

        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_reminder_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminderRequest
        )
    }
}
