package com.expensemate.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.expensemate.app.MainActivity
import com.expensemate.app.R

object NotificationHelper {

    const val CHANNEL_REMINDERS_ID = "channel_daily_reminders"
    const val CHANNEL_BUDGET_ID = "channel_budget_alerts"

    const val NOTIFICATION_REMINDER_ID = 1001
    const val NOTIFICATION_BUDGET_BASE_ID = 2000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                context.getString(R.string.channel_reminders_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_reminders_desc)
            }

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ID,
                context.getString(R.string.channel_budget_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_budget_desc)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, budgetChannel))
        }
    }

    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Track Today's Expenses")
            .setContentText("Don't forget to record your expenses and income for today in ExpenseMate!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_REMINDER_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted by user yet
        }
    }

    fun showBudgetAlert(context: Context, categoryName: String, percentage: Float, isOverBudget: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isOverBudget) "Budget Exceeded!" else "Budget Warning (80% Reached)"
        val message = if (isOverBudget) {
            "You have exceeded 100% of your budget for $categoryName."
        } else {
            "You have used ${String.format(java.util.Locale.US, "%.0f", percentage)}% of your budget for $categoryName."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val notificationId = NOTIFICATION_BUDGET_BASE_ID + (categoryName.hashCode() % 100)
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
}
