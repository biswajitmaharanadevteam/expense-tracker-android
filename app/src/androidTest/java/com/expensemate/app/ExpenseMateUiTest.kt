package com.expensemate.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.ui.components.EmptyState
import com.expensemate.app.ui.components.TotalBalanceCard
import com.expensemate.app.ui.components.TransactionRow
import com.expensemate.app.ui.theme.ExpenseMateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseMateUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testTotalBalanceCardDisplaysCorrectValues() {
        composeTestRule.setContent {
            ExpenseMateTheme {
                TotalBalanceCard(
                    balance = 45000.0,
                    periodIncome = 60000.0,
                    periodExpense = 15000.0,
                    currency = CurrencyConfig.DEFAULT
                )
            }
        }

        composeTestRule.onNodeWithText("Total Balance").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹45,000.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expense").assertIsDisplayed()
    }

    @Test
    fun testTransactionRowDisplaysDetails() {
        val testTx = Transaction(
            id = 1,
            title = "Organic Market",
            amount = 1250.0,
            type = TransactionType.EXPENSE,
            categoryId = 2,
            categoryName = "Groceries",
            accountId = 1,
            accountName = "Cash",
            paymentMethod = PaymentMethod.CASH,
            date = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            ExpenseMateTheme {
                TransactionRow(
                    transaction = testTx,
                    currency = CurrencyConfig.DEFAULT,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Organic Market").assertIsDisplayed()
        composeTestRule.onNodeWithText("Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("-₹1,250.00").assertIsDisplayed()
    }

    @Test
    fun testEmptyStateDisplaysPrompt() {
        composeTestRule.setContent {
            ExpenseMateTheme {
                EmptyState(
                    title = "No transactions found",
                    description = "Tap the + button to record your first income or expense."
                )
            }
        }

        composeTestRule.onNodeWithText("No transactions found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap the + button to record your first income or expense.").assertIsDisplayed()
    }
}
