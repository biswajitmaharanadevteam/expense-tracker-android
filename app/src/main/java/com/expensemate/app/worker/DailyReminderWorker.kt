package com.expensemate.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensemate.app.data.preferences.UserPreferencesManager
import com.expensemate.app.notifications.NotificationHelper
import kotlinx.coroutines.flow.first

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val preferencesManager = UserPreferencesManager(context)
        val settings = preferencesManager.appSettingsFlow.first()

        if (settings.dailyReminderEnabled) {
            NotificationHelper.showDailyReminder(context)
        }

        return Result.success()
    }
}
