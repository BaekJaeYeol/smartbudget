package com.yu_morph.smartbudget.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionCategoryUiTest {
    @Test
    fun `모든 거래 카테고리는 서로 다른 색상을 사용한다`() {
        val colors = transactionCategoryCodes.map { transactionCategoryUi(it).color.value }

        assertEquals(12, transactionCategoryCodes.size)
        assertEquals(colors.size, colors.distinct().size)
    }
}
