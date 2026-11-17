package com.yu_morph.smartbudget.ui.main

import androidx.compose.ui.graphics.Color
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

    @Test
    fun `예산 사용률 구간에 맞는 색상을 반환한다`() {
        assertEquals(Color(0xFF17A673), budgetUsageColor(0.5f))
        assertEquals(Color(0xFFF2C94C), budgetUsageColor(0.51f))
        assertEquals(Color(0xFFF2994A), budgetUsageColor(0.71f))
        assertEquals(Color(0xFFEB5757), budgetUsageColor(0.91f))
        assertEquals(Color(0xFFEB5757), budgetUsageColor(1.2f))
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
