package com.expensemate.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    /**
     * Example migration for adding receiptImageUri or future fields cleanly
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Adds receiptImageUri to transactions table in future schema version 2
            db.execSQL("ALTER TABLE transactions ADD COLUMN receiptImageUri TEXT DEFAULT NULL")
        }
    }
}
