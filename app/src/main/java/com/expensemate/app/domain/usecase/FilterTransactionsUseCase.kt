package com.expensemate.app.domain.usecase

import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType

enum class TransactionSortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount")
}

data class TransactionFilterCriteria(
    val query: String = "",
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val paymentMethod: PaymentMethod? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val sortOption: TransactionSortOption = TransactionSortOption.DATE_DESC
)

class FilterTransactionsUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        criteria: TransactionFilterCriteria
    ): List<Transaction> {
        return transactions.asSequence()
            .filter { tx ->
                if (criteria.query.isBlank()) true
                else {
                    val q = criteria.query.trim().lowercase()
                    tx.title.lowercase().contains(q) ||
                            tx.payeeOrMerchant.lowercase().contains(q) ||
                            tx.notes.lowercase().contains(q) ||
                            tx.categoryName.lowercase().contains(q) ||
                            tx.tags.any { it.lowercase().contains(q) }
                }
            }
            .filter { tx -> criteria.type == null || tx.type == criteria.type }
            .filter { tx -> criteria.categoryId == null || tx.categoryId == criteria.categoryId }
            .filter { tx -> criteria.accountId == null || tx.accountId == criteria.accountId }
            .filter { tx -> criteria.paymentMethod == null || tx.paymentMethod == criteria.paymentMethod }
            .filter { tx -> criteria.startDate == null || tx.date >= criteria.startDate }
            .filter { tx -> criteria.endDate == null || tx.date <= criteria.endDate }
            .sortedWith(
                when (criteria.sortOption) {
                    TransactionSortOption.DATE_DESC -> compareByDescending { it.date }
                    TransactionSortOption.DATE_ASC -> compareBy { it.date }
                    TransactionSortOption.AMOUNT_DESC -> compareByDescending { it.amount }
                    TransactionSortOption.AMOUNT_ASC -> compareBy { it.amount }
                }
            )
            .toList()
    }
}
