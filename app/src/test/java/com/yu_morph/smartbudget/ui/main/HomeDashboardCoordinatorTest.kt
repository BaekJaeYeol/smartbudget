package com.yu_morph.smartbudget.ui.main

import com.yu_morph.smartbudget.data.remote.model.TransactionData
import com.yu_morph.smartbudget.data.remote.model.TransactionSource
import com.yu_morph.smartbudget.data.remote.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDashboardCoordinatorTest {
    @Test
    fun `수입과 지출을 각각 합산한다`() {
        val transactions =
            listOf(
                transaction(id = 1, type = TransactionType.INCOME, amount = 3_000_000),
                transaction(id = 2, type = TransactionType.EXPENSE, amount = 450_000),
                transaction(id = 3, type = TransactionType.EXPENSE, amount = 50_000),
            )

        assertEquals(HomeMonthlyAmounts(income = 3_000_000, expense = 500_000), summarizeHomeTransactions(transactions))
    }

    private fun transaction(id: Int, type: TransactionType, amount: Int) = TransactionData(
        id = id,
        date = "2026-11-17",
        type = type,
        source = TransactionSource.MANUAL,
        category = "food",
        amount = amount,
        createdAt = "2026-11-17T10:00:00+09:00",
        updatedAt = "2026-11-17T10:00:00+09:00",
    )
}
