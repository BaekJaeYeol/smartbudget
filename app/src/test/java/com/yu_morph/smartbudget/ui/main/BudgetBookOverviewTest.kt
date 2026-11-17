package com.yu_morph.smartbudget.ui.main

import com.yu_morph.smartbudget.data.remote.model.TransactionData
import com.yu_morph.smartbudget.data.remote.model.TransactionSource
import com.yu_morph.smartbudget.data.remote.model.TransactionType
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetBookOverviewTest {
    @Test
    fun `거래 내역을 년월별로 분류한다`() {
        val transactions =
            listOf(
                transaction(id = 1, date = "2026-10-31", type = TransactionType.EXPENSE, amount = 10_000),
                transaction(id = 2, date = "2026-11-01", type = TransactionType.INCOME, amount = 20_000),
            )

        val grouped = groupTransactionsByMonth(transactions)

        assertEquals(listOf(1), grouped.getValue(YearMonth.of(2026, 10)).map(TransactionData::id))
        assertEquals(listOf(2), grouped.getValue(YearMonth.of(2026, 11)).map(TransactionData::id))
    }

    @Test
    fun `월별 수입과 지출을 각각 합산한다`() {
        val transactions =
            listOf(
                transaction(id = 1, type = TransactionType.INCOME, amount = 3_000_000),
                transaction(id = 2, type = TransactionType.INCOME, amount = 200_000),
                transaction(id = 3, type = TransactionType.EXPENSE, amount = 1_100_000),
            )

        assertEquals(
            MonthlyTransactionAmounts(income = 3_200_000, expense = 1_100_000),
            monthlyTransactionAmounts(transactions),
        )
    }

    private fun transaction(id: Int, date: String = "2026-11-01", type: TransactionType, amount: Int) = TransactionData(
        id = id,
        date = date,
        type = type,
        source = TransactionSource.MANUAL,
        category = if (type == TransactionType.INCOME) "salary" else "food",
        amount = amount,
        createdAt = "2026-11-01T00:00:00+09:00",
        updatedAt = "2026-11-01T00:00:00+09:00",
    )
}
