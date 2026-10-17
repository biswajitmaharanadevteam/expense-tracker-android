package com.expensemate.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensemate.app.data.local.ExpenseMateDatabase
import com.expensemate.app.data.preferences.UserPreferencesManager
import com.expensemate.app.data.repository.ExpenseMateRepositoryImpl
import com.expensemate.app.domain.usecase.ProcessRecurringTransactionsUseCase

class RecurringTransactionWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = ExpenseMateDatabase.getInstance(context)
            val preferencesManager = UserPreferencesManager(context)
            val repository = ExpenseMateRepositoryImpl(database, preferencesManager)

            val activeRules = repository.getActiveRecurringRules()
            if (activeRules.isEmpty()) {
                return Result.success()
            }

            val processUseCase = ProcessRecurringTransactionsUseCase()
            val result = processUseCase(activeRules)

            // Insert generated transactions
            if (result.newTransactions.isNotEmpty()) {
                repository.insertTransactions(result.newTransactions)
            }

            // Update rule timestamps
            for (rule in result.updatedRules) {
                repository.updateRecurringRule(rule)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
