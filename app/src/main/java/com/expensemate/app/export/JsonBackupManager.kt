package com.expensemate.app.export

import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.Budget
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.RecurringRule
import com.expensemate.app.domain.model.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

data class BackupData(
    val exportTimestamp: Long,
    val appVersion: String,
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val accounts: List<Account>,
    val budgets: List<Budget>,
    val recurringRules: List<RecurringRule>
)

object JsonBackupManager {

    fun exportBackup(backupData: BackupData, outputStream: OutputStream) {
        val root = JSONObject()
        root.put("version", "1.0.0")
        root.put("timestamp", backupData.exportTimestamp)

        // Categories
        val categoriesArray = JSONArray()
        for (cat in backupData.categories) {
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("type", cat.type.name)
            obj.put("iconName", cat.iconName)
            obj.put("colorHex", cat.colorHex)
            obj.put("isDefault", cat.isDefault)
            categoriesArray.put(obj)
        }
        root.put("categories", categoriesArray)

        // Accounts
        val accountsArray = JSONArray()
        for (acc in backupData.accounts) {
            val obj = JSONObject()
            obj.put("id", acc.id)
            obj.put("name", acc.name)
            obj.put("type", acc.type.name)
            obj.put("initialBalance", acc.initialBalance)
            obj.put("colorHex", acc.colorHex)
            obj.put("isDefault", acc.isDefault)
            accountsArray.put(obj)
        }
        root.put("accounts", accountsArray)

        // Transactions
        val transactionsArray = JSONArray()
        for (tx in backupData.transactions) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("type", tx.type.name)
            obj.put("categoryId", tx.categoryId)
            obj.put("accountId", tx.accountId)
            obj.put("paymentMethod", tx.paymentMethod.name)
            obj.put("date", tx.date)
            obj.put("payeeOrMerchant", tx.payeeOrMerchant)
            obj.put("notes", tx.notes)
            obj.put("tags", JSONArray(tx.tags))
            obj.put("isRecurring", tx.isRecurring)
            tx.recurringRuleId?.let { obj.put("recurringRuleId", it) }
            transactionsArray.put(obj)
        }
        root.put("transactions", transactionsArray)

        // Budgets
        val budgetsArray = JSONArray()
        for (b in backupData.budgets) {
            val obj = JSONObject()
            obj.put("id", b.id)
            b.categoryId?.let { obj.put("categoryId", it) }
            obj.put("month", b.month)
            obj.put("year", b.year)
            obj.put("amountLimit", b.amountLimit)
            budgetsArray.put(obj)
        }
        root.put("budgets", budgetsArray)

        val writer = OutputStreamWriter(outputStream)
        writer.write(root.toString(2))
        writer.flush()
    }

    fun parseBackup(inputStream: InputStream): BackupData {
        val jsonText = inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonText)

        val timestamp = root.optLong("timestamp", System.currentTimeMillis())
        val version = root.optString("version", "1.0.0")

        // Parse Categories
        val categories = mutableListOf<Category>()
        val categoriesArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until categoriesArray.length()) {
            val obj = categoriesArray.getJSONObject(i)
            categories.add(
                Category(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    type = com.expensemate.app.domain.model.TransactionType.valueOf(obj.getString("type")),
                    iconName = obj.optString("iconName", "category"),
                    colorHex = obj.optString("colorHex", "#2A3B90"),
                    isDefault = obj.optBoolean("isDefault", false)
                )
            )
        }

        // Parse Accounts
        val accounts = mutableListOf<Account>()
        val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
        for (i in 0 until accountsArray.length()) {
            val obj = accountsArray.getJSONObject(i)
            accounts.add(
                Account(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    type = com.expensemate.app.domain.model.AccountType.valueOf(obj.getString("type")),
                    initialBalance = obj.optDouble("initialBalance", 0.0),
                    currentBalance = obj.optDouble("initialBalance", 0.0),
                    colorHex = obj.optString("colorHex", "#2A3B90"),
                    isDefault = obj.optBoolean("isDefault", false)
                )
            )
        }

        // Parse Transactions
        val transactions = mutableListOf<Transaction>()
        val transactionsArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until transactionsArray.length()) {
            val obj = transactionsArray.getJSONObject(i)
            val tagsArray = obj.optJSONArray("tags") ?: JSONArray()
            val tags = mutableListOf<String>()
            for (j in 0 until tagsArray.length()) {
                tags.add(tagsArray.getString(j))
            }

            transactions.add(
                Transaction(
                    id = obj.optLong("id", 0),
                    title = obj.getString("title"),
                    amount = obj.getDouble("amount"),
                    type = com.expensemate.app.domain.model.TransactionType.valueOf(obj.getString("type")),
                    categoryId = obj.getLong("categoryId"),
                    accountId = obj.getLong("accountId"),
                    paymentMethod = try {
                        com.expensemate.app.domain.model.PaymentMethod.valueOf(obj.getString("paymentMethod"))
                    } catch (e: Exception) {
                        com.expensemate.app.domain.model.PaymentMethod.CASH
                    },
                    date = obj.getLong("date"),
                    payeeOrMerchant = obj.optString("payeeOrMerchant", ""),
                    notes = obj.optString("notes", ""),
                    tags = tags,
                    isRecurring = obj.optBoolean("isRecurring", false),
                    recurringRuleId = if (obj.has("recurringRuleId")) obj.getLong("recurringRuleId") else null
                )
            )
        }

        // Parse Budgets
        val budgets = mutableListOf<Budget>()
        val budgetsArray = root.optJSONArray("budgets") ?: JSONArray()
        for (i in 0 until budgetsArray.length()) {
            val obj = budgetsArray.getJSONObject(i)
            budgets.add(
                Budget(
                    id = obj.optLong("id", 0),
                    categoryId = if (obj.has("categoryId")) obj.getLong("categoryId") else null,
                    month = obj.getInt("month"),
                    year = obj.getInt("year"),
                    amountLimit = obj.getDouble("amountLimit")
                )
            )
        }

        return BackupData(
            exportTimestamp = timestamp,
            appVersion = version,
            transactions = transactions,
            categories = categories,
            accounts = accounts,
            budgets = budgets,
            recurringRules = emptyList()
        )
    }
}
