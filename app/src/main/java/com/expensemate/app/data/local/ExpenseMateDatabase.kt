package com.expensemate.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expensemate.app.data.local.dao.AccountDao
import com.expensemate.app.data.local.dao.BudgetDao
import com.expensemate.app.data.local.dao.CategoryDao
import com.expensemate.app.data.local.dao.RecurringRuleDao
import com.expensemate.app.data.local.dao.TransactionDao
import com.expensemate.app.data.local.entity.AccountEntity
import com.expensemate.app.data.local.entity.BudgetEntity
import com.expensemate.app.data.local.entity.CategoryEntity
import com.expensemate.app.data.local.entity.RecurringRuleEntity
import com.expensemate.app.data.local.entity.TransactionEntity
import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ExpenseMateDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseMateDatabase? = null

        fun getInstance(context: Context): ExpenseMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseMateDatabase::class.java,
                    "expensemate.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default categories and accounts on background dispatcher
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                val initialCategories = (Category.DEFAULT_EXPENSE_CATEGORIES + Category.DEFAULT_INCOME_CATEGORIES)
                                    .map { CategoryEntity.fromDomain(it) }
                                database.categoryDao().insertCategories(initialCategories)

                                val initialAccounts = Account.DEFAULT_ACCOUNTS
                                    .map { AccountEntity.fromDomain(it) }
                                database.accountDao().insertAccounts(initialAccounts)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
